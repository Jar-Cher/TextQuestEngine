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
import javax.json.*
import javax.json.JsonObject

val gson = Gson()

class Quest : JsonModel {
    val saveProperty = SimpleObjectProperty<GameState>(GameState())
    var save by saveProperty
    val startingProperty = SimpleObjectProperty<GameState>(GameState())
    var starting by startingProperty
    val finishingIdProperty = SimpleIntegerProperty(1)
    var finishingId by finishingIdProperty

    val slides = FXCollections.observableArrayList<Slide>()

    fun findWithID(id: Int): Slide {
        for(i in slides)
            if (i.slideId == id)
                return i
        throw IllegalArgumentException("Error: No Slide with such ID exists.")
    }

    override fun updateModel(json: JsonObject) {
        with(json) {
            //save = GameState().updateModel(jsonObject("save"))
            //starting = string("firstName")
            finishingId = int("finishingId") ?: 0
            slides.setAll(getJsonArray("slides").toModel())
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
}


class Slide : JsonModel {
    val optionsProperty = SimpleObjectProperty<Array<Option>>(emptyArray())
    var options by optionsProperty
    val textProperty = SimpleStringProperty("Click \"load\" to start a game.")
    var text by textProperty
    val slideIdProperty = SimpleIntegerProperty(0)
    var slideId by slideIdProperty
}


class Option : JsonModel {
    val moveToSlideProperty = SimpleIntegerProperty()
    var moveToSlide by moveToSlideProperty
    val optionTextProperty = SimpleStringProperty()
    var optionText by optionTextProperty
    val optionIdProperty = SimpleIntegerProperty()
    var optionId by optionIdProperty
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
var slideText = quest.findWithID(currentSlideID).textProperty.stringBinding { it }

class MainView : View("Text Engine") {

    init {
        currentSlideID = 0
        //slideText = SimpleStringProperty(quest.getText(currentSlideID))
    }

    override val root = borderpane {

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

                label(slideText.value) {
                    prefWidth = 580.0
                    isWrapText = true
                    translateX = 10.0
                    translateY = 10.0
                }.bind(slideText)
            }
        }

        bottom {
            for (i in 1..quest.findWithID(currentSlideID).options.size){
                button(i.toString()) {

                }

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
    val input = SimpleStringProperty()

    override val root = form {
        fieldset {
            field("Name of a savefile:") {
                textfield(input)
            }

            button("Load") {
                action {
                    controller.loadSave(input.value)
                    input.value = ""
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
    fun loadSave(inputValue: String) {

        val json2 = File(inputValue).readText()
        quest = gson.fromJson(json2, Quest::class.java)

        isQuestLoaded.value = true
        println(quest)

        /*find<MainView>().setCurrentSlide(quest.save.slideId)
        find<MainView>().setText()
        slideText = SimpleStringProperty("an")*/
    }

    fun createSave(inputValue: String) {

        println("success!")
    }
}

class HashView : View("My View") {
    val hashProperty = SimpleStringProperty("EMPTY")

    override val root = vbox {
        textfield {
            hashProperty.bind(stringBinding(textProperty()) { this.value }) // This does not work
        }
        label(hashProperty)
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
