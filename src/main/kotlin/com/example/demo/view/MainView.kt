package com.example.demo.view

import com.example.demo.app.Styles
import com.google.gson.*
import com.sun.javafx.binding.ContentBinding.bind
import groovy.util.Eval
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleObjectProperty
import tornadofx.*
import javafx.beans.property.SimpleStringProperty
import javafx.collections.FXCollections
import javafx.scene.input.*
import javafx.scene.layout.Priority
import javafx.stage.Modality
import java.io.File
import tornadofx.getValue
import tornadofx.setValue
import java.io.FileInputStream
import javax.json.*
import javax.json.JsonObject
import javafx.stage.FileChooser
import javafx.scene.control.TextField

//val gson = Gson()

class Quest : JsonModel {
    val saveProperty = SimpleObjectProperty<GameState>(GameState())
    var save by saveProperty
    val startingProperty = SimpleObjectProperty<GameState>(GameState())
    var starting by startingProperty
    val finishingIdProperty = SimpleIntegerProperty(1)
    var finishingId by finishingIdProperty

    val slides = FXCollections.observableArrayList<Slide>(Slide())

    fun findWithID(id: Int): Slide {
        for(i in slides)
            if (i.slideId == id)
                return i
        throw IllegalArgumentException("Error: No Slide with such ID exists.")
    }

    override fun updateModel(json: JsonObject) {
        with(json) {
            save = jsonModel("save")
            starting = jsonModel("starting")
            finishingId = int("finishingId") ?: 0
            slides.clear()
            slides.addAll(getJsonArray("slides").toModel())
        }
    }

    override fun toJSON(json: JsonBuilder) {
        with(json) {
            add("save", save.toJSON())
            add("starting", starting.toJSON())
            add("finishingId", finishingId)
            add("slides", slides.toJSON())
        }
    }

    fun getSaveSlide() : GameState = save

    fun getStartingSlide() : GameState = starting

    fun getFinishingID() : Int = finishingId

    fun getText(i : Int) : String = findWithID(i).text
}

class GameState : JsonModel {
    val slideIdProperty = SimpleIntegerProperty(0)
    var slideId by slideIdProperty

    override fun updateModel(json: JsonObject) {
        with(json) {
            slideId = int("slideId") ?: 0
        }
    }

    override fun toJSON(json: JsonBuilder) {
        with(json) {
            add("slideId", slideId)
        }
    }

}


class Slide : JsonModel {
    val options = FXCollections.observableArrayList<Option>()

    val textProperty = SimpleStringProperty("Click \"load\" to start a game.")
    var text by textProperty
    val slideIdProperty = SimpleIntegerProperty(0)
    var slideId by slideIdProperty

    override fun updateModel(json: JsonObject) {
        with(json) {
            text = string("text")
            slideId = int("slideId") ?: 0

            options.clear()
            options.addAll(getJsonArray("options").toModel())
        }
    }

    override fun toJSON(json: JsonBuilder) {
        with(json) {
            add("options", options.toJSON())
            add("text", text)
            add("slideId", slideId)
        }
    }
}


class Option : JsonModel {
    val moveToSlideProperty = SimpleIntegerProperty()
    var moveToSlide by moveToSlideProperty
    val optionTextProperty = SimpleStringProperty()
    var optionText by optionTextProperty
    val optionIdProperty = SimpleIntegerProperty()
    var optionId by optionIdProperty

    override fun updateModel(json: JsonObject) {
        with(json) {
            optionText = string("optionText")
            moveToSlide = int("moveToSlide") ?: 0
            optionId = int("optionId") ?: 0
        }
    }

    override fun toJSON(json: JsonBuilder) {
        with(json) {
            add("optionText", optionText)
            add("moveToSlide", moveToSlide)
            add("optionId", optionId)
        }
    }
}


class Available : JsonModel {
    val stateProperty = SimpleObjectProperty<Map<String, Any>>()
    var state by stateProperty
    val idProperty = SimpleIntegerProperty()
    var id by idProperty
}


var quest = Quest()
val isQuestLoaded = SimpleBooleanProperty(false)
var currentSlideID = 0
// var slideText = quest.findWithID(currentSlideID).textProperty.stringBinding { it }

class MainView : View("Text Engine") {

    override val root = borderpane()

    init {
        with (root) {
            top {
                vbox {
                    hbox {
                        button("load") {
                            action {
                                find<LoadMenu>().openWindow(modality = Modality.WINDOW_MODAL)
                            }
                        }

                        button("save") {
                            enableWhen(isQuestLoaded)

                            action {
                                find<SaveMenu>().openWindow(modality = Modality.WINDOW_MODAL)
                            }
                        }

                        button("restart") {
                            enableWhen(isQuestLoaded)
                        }

                    }

                    line {
                        startX = 0.0
                        startY = 0.0
                        endX = 600.0
                        endY = 0.0
                    }

                    label(com.example.demo.view.quest.findWithID(currentSlideID).text) {
                        prefWidth = 580.0
                        isWrapText = true
                        translateX = 10.0
                        translateY = 10.0
                    }
                }
            }

            bottom {
                /*for (i in 1..quest.findWithID(currentSlideID).options.size){
                    button(i.toString()) {

                    }

                }*/
            }
        }
    }


    /*fun setText(newText: String = quest.getText(currentSlideID)) {
        slideText = SimpleStringProperty(newText)

    }

    fun setCurrentSlide(id: Int) {
        currentSlideID = id
    }*/
}

class LoadMenu : View("Load game") {
    val controller: MyController by inject()
    lateinit var inputTextField: TextField
    var input: List<File> = emptyList()
    //var inputName = SimpleStringProperty()
    private val extensions
            = arrayOf(FileChooser.ExtensionFilter("Any file", "*"))

    override val root = form {
        fieldset {
            field("Name of a savefile:") {
                inputTextField = textfield()
                button("Choose in explorer") {
                    action {
                        input = chooseFile("Select quest file", extensions, FileChooserMode.Single)
                        if (input.isNotEmpty()) {
                            inputTextField.text = "${input.first()}"
                        }
                    }
                }
            }

            button("Load") {
                action {
                    if (input.isNotEmpty())
                        controller.loadSave(input.first())
                    else {
                        controller.loadSave(File(inputTextField.text))
                    }
                    close()
                }
                shortcut(KeyCombination.valueOf("Enter"))
            }
        }
    }

}

class SaveMenu : View("Save game") {
    val controller: MyController by inject()
    val input = SimpleStringProperty()

    override val root = form {
        fieldset {
            field("Name your savefile:") {
                textfield(input)
            }

            button("Save") {
                action {
                    controller.createSave(input.value)
                    input.value = ""
                    close()
                }
                shortcut(KeyCombination.valueOf("Enter"))
            }
        }
    }

}

class MyController : Controller() {
    fun loadSave(input: File) {

        val jsonFile = input.inputStream()
        val reader = Json.createReader(jsonFile)
        quest.updateModel(reader.readObject())
        reader.close()

        currentSlideID++
        isQuestLoaded.value = true
        println(quest.finishingId)

        /*find<MainView>().setCurrentSlide(quest.save.slideId)
        find<MainView>().setText()
        slideText = SimpleStringProperty("an")*/
    }

    fun createSave(inputValue: String) {

        println("success!")
    }
}


val inputProperty = SimpleStringProperty("a")
val hashProperty = inputProperty.stringBinding { it }

class HushView : View("My View") {

    override val root = vbox {
        textfield(inputProperty)
        label(hashProperty)
    }

}
