package com.example.demo.view

import com.example.demo.app.Styles
import com.google.gson.*
import com.google.gson.internal.LinkedTreeMap
import com.sun.javafx.binding.ContentBinding.bind
import groovy.util.Eval
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleObjectProperty
import tornadofx.*
import javafx.beans.property.SimpleStringProperty
import javafx.collections.FXCollections
import javafx.scene.control.Label
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
import javafx.scene.layout.BorderPane
import javafx.scene.layout.VBox
import java.util.*
import com.google.gson.reflect.TypeToken
import javafx.collections.ObservableMap
import jdk.nashorn.internal.runtime.PropertyHashMap


val gson = Gson()


class Quest : JsonModel, ItemViewModel<Quest>() {
    val saveProperty = SimpleObjectProperty<GameState>(GameState())
    var save by saveProperty
    val startingProperty = SimpleObjectProperty<GameState>(GameState())
    var starting by startingProperty
    val finishingIdProperty = SimpleIntegerProperty(1)
    var finishingId by finishingIdProperty
    val slides = FXCollections.observableArrayList<Slide>(Slide())

    fun findWithID(id: Int): Slide {
        for (i in slides)
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

}


class GameState : JsonModel {
    val slideIdProperty = SimpleIntegerProperty(0)
    var slideId by slideIdProperty
    var gameData = mutableMapOf<String, Any>()
    val gameDataMapType = object : TypeToken<Map<String,Any>>() {}.type

    override fun updateModel(json: JsonObject) {
        with(json) {
            slideId = int("slideId") ?: 0
            gameData.clear()
            gameData = gson.fromJson(jsonObject("gameData").toString(), gameDataMapType)
        }
    }

    override fun toJSON(json: JsonBuilder) {
        with(json) {
            add("slideId", slideId)
            add("gameData", gson.toJson(gameData))
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
    val availableProperty = SimpleStringProperty()
    var available by availableProperty
    var newGameData = mutableMapOf<String, Any>().observable()
    val GameDataMapType = object : TypeToken<Map<String,Any>>() {}.type
    var changeGameData = mutableMapOf<String, Any>().observable()

    override fun updateModel(json: JsonObject) {
        with(json) {
            optionText = string("optionText")
            moveToSlide = int("moveToSlide") ?: 0
            optionId = int("optionId") ?: 0
            available = string("available") ?: "true"
            newGameData.clear()
            newGameData = gson.fromJson(jsonObject("newGameData").toString(), GameDataMapType)
            changeGameData.clear()
            changeGameData = gson.fromJson(jsonObject("newGameData").toString(), GameDataMapType)
        }
    }

    override fun toJSON(json: JsonBuilder) {
        with(json) {
            add("optionText", optionText)
            add("moveToSlide", moveToSlide)
            add("optionId", optionId)
            add("available", available)
            add("newGameData", gson.toJson(newGameData))
            add("changeGameData", gson.toJson(changeGameData))
        }
    }
}


/*class Available : JsonModel {
    val stateProperty = SimpleObjectProperty<Map<String, Any>>()
    var state by stateProperty
    val idProperty = SimpleIntegerProperty()
    var id by idProperty
}*/


/*class questModel(quest: Quest) : ItemViewModel<Quest>(quest) {
    val isQuestLoaded = SimpleBooleanProperty(false)
    val save = bind(Quest::saveProperty)
    val starting = bind(Quest::startingProperty)
    val finishingId = bind(Quest::finishingIdProperty)
    val slides = bind(Quest::slides)
}*/


class MainView : View("Text Engine") {

    override val root = borderpane()

    val quest = Quest()
    val isQuestLoaded = SimpleBooleanProperty(false)
    lateinit var mainText: Label
    lateinit var buttonPanel: VBox

    init {
        with(root) {
            top {
                vbox {
                    hbox {
                        button("load") {
                            action {
                                find<LoadMenu>().openWindow(modality = Modality.APPLICATION_MODAL)
                            }
                        }

                        button("save") {
                            enableWhen(isQuestLoaded)

                            action {
                                find<SaveMenu>().openWindow(modality = Modality.APPLICATION_MODAL)
                            }
                        }

                        button("restart") {
                            enableWhen(isQuestLoaded)

                            action {
                                find<ReloadMenu>().openWindow(modality = Modality.APPLICATION_MODAL)
                            }
                        }

                    }

                    line {
                        startX = 0.0
                        startY = 0.0
                        endX = 600.0
                        endY = 0.0
                    }

                    mainText = label(quest.findWithID(quest.save.slideId).text) {
                        prefWidth = 580.0
                        isWrapText = true
                        translateX = 10.0
                        translateY = 10.0
                    }
                }
            }

            bottom {
                buttonPanel = vbox (2) {

                    translateX = 5.0
                    translateY = -5.0

                }
            }
        }
    }

    fun update() {

        val currentSlide = quest.findWithID(quest.save.slideId)
        mainText.text = currentSlide.text
        buttonPanel.clear()

        for (i in currentSlide.options) {

            val newButton = button(i.optionText) {
                action {
                    if (i.moveToSlide == quest.finishingId)
                        close()
                    else {
                        quest.save.slideId = i.moveToSlide
                        update()
                    }
                }
            }

            buttonPanel.add(newButton)
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
    private val extensions = arrayOf(FileChooser.ExtensionFilter("Any file", "*"))

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
    lateinit var inputDirectoryTextField: TextField
    lateinit var inputFileNameTextField: TextField

    override val root = form {
        fieldset {
            field("Specify directory of the savefile, \nor leave the field empty to save in default directory:") {
                inputDirectoryTextField = textfield()
                button("Choose in explorer") {
                    isWrapText = true
                    action {
                        val input = chooseDirectory("Select quest file")
                        if (input != null) {
                            inputDirectoryTextField.text = "$input"
                        }
                    }
                }
            }

            field("Name your savefile:") {
                inputFileNameTextField = textfield("game.txt")
            }

            button("Save") {
                action {

                    if (inputDirectoryTextField.text.isNotEmpty())
                        controller.createSave(File(inputDirectoryTextField.text + "\\" +
                                inputFileNameTextField.text))
                    else
                        controller.createSave(File(inputFileNameTextField.text))
                    close()
                }
                shortcut(KeyCombination.valueOf("Enter"))
            }
        }
    }

}


class ReloadMenu : View("Reload game?") {
    val controller: MyController by inject()

    override val root = vbox(10) {
        label("Your progress won't be saved!")
        minWidth = 220.0

        borderpane {
            left = button("Yep") {
                action {
                    controller.reloadGame()
                    close()
                }
            }

            right = button("Nope") {
                action {
                    close()
                }
            }
        }
    }
}


class MyController : Controller() {

    val mainView: MainView by inject()

    fun loadSave(input: File) {

        val jsonFile = input.inputStream()
        val reader = Json.createReader(jsonFile)

        mainView.quest.updateModel(reader.readObject())
        reader.close()

        mainView.isQuestLoaded.value = true
        mainView.update()

    }

    fun createSave(outputFile: File) {

        val writer = outputFile.bufferedWriter()
        writer.write(mainView.quest.toJSON().toString())
        writer.close()

    }

    fun reloadGame() {

        mainView.quest.save = mainView.quest.starting
        mainView.update()

    }
}


/*
val inputProperty = SimpleStringProperty("a")
val hashProperty = inputProperty.stringBinding { it }

class HushView : View("My View") {

    override val root = vbox {
        textfield(inputProperty)
        label(hashProperty)
    }

}
*/