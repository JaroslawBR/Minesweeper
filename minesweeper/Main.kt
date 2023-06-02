package minesweeper

import kotlin.random.Random

fun main() {
    Mines().seatMines()
}

class Mines(column: Int = 9, rows: Int = 9){
    private val minesColumn = column
    private val minesRows = rows

    private val warField = MutableList(minesColumn*minesRows)  { "." }
    private var fullSymbolField: MutableList<MutableList<Any>> = MutableList(minesRows ) { MutableList(minesColumn) {"."} }

    private fun randomMines(): HashSet<Int> {
        print("How many mines do you want on the field? ")
        try {
            val minesNumber = readlnOrNull()?.toIntOrNull()
            if (minesNumber != null) {
                throw1(minesNumber)
                val defaultGenerator = Random.Default
                val setRandomNumber = HashSet<Int>(minesNumber)
                while (setRandomNumber.size < minesNumber) {
                    setRandomNumber += defaultGenerator.nextInt(0, minesColumn * minesRows)
                }
                return setRandomNumber
            } else {
                throw IllegalArgumentException("Wrong input format.")
            }
        } catch (e: IndexOutOfBoundsException) {
            println(e.message)
            return randomMines()
        } catch (e: IllegalArgumentException) {
            println(e.message)
            return randomMines()
        } catch (e: Exception) {
            println("Wrong input format")
            return randomMines()
        }
    }

    fun seatMines() {
        val mainLocation = randomMines()
        for (i in mainLocation) {
            warField[i] = "X"
        }
        minesAround()
    }

    private fun formatField(): MutableList<MutableList<String>> {
        return warField.chunked(minesRows).map { it.toMutableList() }.toMutableList()
    }

    private fun throw1(minesNumber: Int){
        if (minesNumber > minesRows * minesColumn - 1) {
            throw IndexOutOfBoundsException("Max mines numbers in this field is ${minesRows * minesColumn - 1}")
        }
        if (minesNumber < 0)
            throw IllegalArgumentException("The number of mines cannot be negative")
    }

    private fun  minesAround() {
        val symbolField = formatField()
        val minesLocation = formatField().mapIndexed { rowIndex, row ->
            row.mapIndexedNotNull { columnIndex, mines ->
                if (mines == "X") {
                    Pair(rowIndex, columnIndex)
                } else {
                    null
                }
            }
        }.flatten()
        val numberList = MutableList(minesColumn) { MutableList(minesRows) { 0 } }

        for (mine in minesLocation) {
            val row = mine.first
            val column = mine.second

            for (rowIndex in (row - 1)..(row + 1)) {
                for (columnIndex in (column - 1)..(column + 1)) {
                    if (rowIndex in 0 until minesColumn && columnIndex in 0 until minesRows && symbolField[rowIndex][columnIndex] != "X") {
                        numberList[rowIndex][columnIndex]++
                    }
                }
            }
        }

        mergedNumAndSymbol(numberList, symbolField, minesLocation)

    }

        private fun mergedNumAndSymbol(numberList: MutableList<MutableList<Int>>, symbolField: MutableList<MutableList<String>>, minesLocation: List<Pair<Int, Int>>) {
            val symbolHide = symbolField.map { subList ->
                subList.map { value -> value.replace("X", ".") }.toMutableList() }.toMutableList()
        val mergedList: List<Any> = numberList.zip(symbolHide).flatMap { (number, string) ->
            number.zip(string).map { (num, str) ->
                if (num == 0) str else num
            }
        }
            val finalField = mergedList.chunked(minesRows).map { it.toMutableList() }.toMutableList()
            printField(finalField)
            mergedNumAndSymbol1(numberList, symbolField)
            play(finalField, minesLocation)


    }

    private fun printField(finalField: MutableList<MutableList<Any>>){
        println()
        print(" |")
        for (i in 1..minesColumn) print(i)
        println("|")
        print("—|")
        for (i in 1..minesColumn) print("—")
        println("|")
        for (i in 0 until minesRows) println("${i+1}|${finalField[i].joinToString("")}|")
        print("—|")
        for (i in 1..minesColumn) print("—")
        println("|")
    }

    private fun play(finalField: MutableList<MutableList<Any>>, minesLocation: List<Pair<Int, Int>>) {
        val setList: MutableList<Pair<Int, Int>> = mutableListOf()
        while (true) {
            print("Set/delete mines marks (x and y coordinates): ")
            try {
                val input = readln().split(" ")
                val firstCord = input[0].toInt()
                val secondCord = input[1].toInt()
                val task = input[2]
                floodFill(secondCord, firstCord, fullSymbolField)
                printField(fullSymbolField)
                println()
                if (firstCord <= minesRows - 1 && secondCord <= minesColumn - 1) {
                    if (finalField[secondCord][firstCord] !in 1..8) {
                        if (finalField[secondCord][firstCord] == ".") {
                            finalField[secondCord][firstCord] = "*"
                            setList.add(secondCord to firstCord)
                        } else {
                            finalField[secondCord][firstCord] = "."
                            setList.remove(secondCord to firstCord)
                        }
                        printField(finalField)
                        if (setList.containsAll(minesLocation) && setList.size == minesLocation.size) {
                            println("Congratulations! You found all the mines!")
                            break
                        }
                    } else println("There is a number here!")
                } else println("Off-board coordinates")
            } catch (e: Exception) { println("Wrong Input!")
            }
        }
    }

    private fun floodFill(row: Int, column: Int, list: MutableList<MutableList<Any>>) {
        val symbolField = list
        if (row < 0 || row >= minesColumn || column < 0 || column >= minesRows) {
            return
        }
        if (symbolField[row][column] == "." || symbolField[row][column] == "*") {
            symbolField[row][column] = "/"
            floodFill(row - 1, column, list) // Up
            floodFill(row + 1, column, list) // Down
            floodFill(row, column - 1, list) // Left
            floodFill(row, column + 1, list) // Right
        }
    }

    private fun mergedNumAndSymbol1(numberList: MutableList<MutableList<Int>>, symbolField: MutableList<MutableList<String>>){
        val mergedList: List<Any> = numberList.zip(symbolField).flatMap { (number, string) ->
            number.zip(string).map { (num, str) ->
                if (num == 0) str else num
            }
        }
        fullSymbolField.clear()
        fullSymbolField = mergedList.chunked(minesRows).map { it.toMutableList() }.toMutableList()

    }
}






