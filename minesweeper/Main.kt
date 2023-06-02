package minesweeper

import kotlin.random.Random
import kotlin.system.exitProcess

fun main() {
    Mines().seatMines()
}

class Mines(column: Int = 9, rows: Int = 9){
    private val minesColumn = column
    private val minesRows = rows

    private val warField = MutableList(minesColumn*minesRows)  { "." }
    private var fullSymbolField: MutableList<MutableList<Any>> = MutableList(minesRows ) { MutableList(minesColumn) {"."} }
    private var finalField: MutableList<MutableList<Any>> = MutableList(minesRows ) { MutableList(minesColumn) {"."} }
    private val setList: MutableList<Pair<Int, Int>> = mutableListOf()
    private var minesLocation: List<Pair<Int, Int>> = listOf()
    private var lostNumList: MutableList<Pair<Int, Int>> = mutableListOf()
    private var visited: MutableList<Pair<Int, Int>> = mutableListOf()
    private var firstValue:Int = 0
    private var secondValue:Int = 0
    private var inputBefore = ""

    private fun randomMines(): HashSet<Int> {
        print("How many mines do you want on the field? ")
        try {
            val minesNumber = readlnOrNull()?.toIntOrNull()
            if (minesNumber != null) {
                throw1(minesNumber)
                val defaultGenerator = Random.Default
                val setRandomNumber = HashSet<Int>(minesNumber + 1)
                printField(fullSymbolField)
                print("Set/unset mines marks or claim a cell as free: ")
                    val input = readln().split(" ")
                    val firstCord = input[0].toInt() - 1
                    val secondCord = input[1].toInt() - 1
                    val third = input[2]
                val saveValue = firstCord * secondCord * fullSymbolField.size
                setRandomNumber  += saveValue
                while (setRandomNumber.size < minesNumber + 1) {
                    setRandomNumber += defaultGenerator.nextInt(0, minesColumn * minesRows)
                }
                setRandomNumber.remove(saveValue)
                firstValue = firstCord
                secondValue = secondCord
                inputBefore = third
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
        minesLocation = formatField().mapIndexed { rowIndex, row ->
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
            val finalSymbolField = mergedList.chunked(minesRows).map { it.toMutableList() }.toMutableList()
            mergedNumAndSymbol1(numberList, symbolField)
            play(finalSymbolField)


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

    private fun play(finalField: MutableList<MutableList<Any>>) {
        if (inputBefore == "free") {
            floodFill(secondValue, firstValue, fullSymbolField)
            if (fullSymbolField[secondValue][firstValue] != ".") lostNumList.add(secondValue to firstValue)
        } else mark(secondValue, firstValue)
        finalMerged()
        while (true) {
            print("Set/unset mines marks or claim a cell as free: ")
            try {
                val input = readln().split(" ")
                val firstCord = input[0].toInt() - 1
                val secondCord = input[1].toInt() - 1
                when (input[2]){
                    "free" -> {
                        floodFill(secondCord, firstCord, fullSymbolField)
                        if (fullSymbolField[secondCord][firstCord] != ".")  lostNumList.add(secondCord to firstCord)
                    }
                    "mine" -> {mark(secondCord, firstCord)}
                }
                println()
                finalMerged()

            } catch (e: Exception) {
                println("Wrong Input!")
            }
        }
    }

    private fun mark(secondCord: Int, firstCord: Int){
        if (firstCord <= minesRows - 1 && secondCord <= minesColumn - 1) {
            when(finalField[secondCord][firstCord]) {
                "*" -> {
                    finalField[secondCord][firstCord] = "."
                    setList.remove(secondCord to firstCord)
                }
                // "/" -> println("There is a free space here!")
                //  "." -> { finalField[secondCord][firstCord] = "*" ;setList.add(secondCord to firstCord) }
                else -> {
                    finalField[secondCord][firstCord] = "*"
                    setList.add(secondCord to firstCord)
                }
                    //println("There is a number here!")  }
            }
            if (setList.containsAll(minesLocation) && setList.size == minesLocation.size) {
                    println("Congratulations! You found all the mines!")
                    printField(finalField)
                    exitProcess(0)
                }
            } else println("Off-board coordinates")
        }

    private fun floodFill(row: Int, column: Int, list: MutableList<MutableList<Any>>) {
        val maxRows = list.size
        val maxColumns = list[0].size

        if (minesLocation.contains(row to column)) {
            lose()
            exitProcess(0)
        }

        if (row < 0 || row >= maxRows || column < 0 || column >= maxColumns) {
            return
        }

        if (visited.contains(row to column)) {
            return
        }

        visited.add(row to column)

        if (list[row][column] != ".") {
            return
        }

        list[row][column] = "/" // mark as a free spot

        floodFill(row - 1, column, list) // Up
        floodFill(row + 1, column, list) // Down
        floodFill(row, column - 1, list) // Left
        floodFill(row, column + 1, list) // Right

        // Additional calls for diagonal directions

        // Up-Left
        if (row - 1 >= 0 && column - 1 >= 0 && list[row - 1][column - 1] == "." && !visited.contains((row - 1) to (column - 1))) {
            floodFill(row - 1, column - 1, list)
        }

        // Up-Right
        if (row - 1 >= 0 && column + 1 < maxColumns && list[row - 1][column + 1] == "." && !visited.contains((row - 1) to (column + 1))) {
            floodFill(row - 1, column + 1, list)
        }

        // Down-Left
        if (row + 1 < maxRows && column - 1 >= 0 && list[row + 1][column - 1] == "." && !visited.contains((row + 1) to (column - 1))) {
            floodFill(row + 1, column - 1, list)
        }

        // Down-Right
        if (row + 1 < maxRows && column + 1 < maxColumns && list[row + 1][column + 1] == "." && !visited.contains((row + 1) to (column + 1))) {
            floodFill(row + 1, column + 1, list)
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
    /*
    private fun finalMerged() {

        fun isIndexValid(rowIndex: Int, colIndex: Int, rows: Int, cols: Int): Boolean {
            return rowIndex in 0 until rows && colIndex >= 0 && colIndex < cols
        }
        val lista2: MutableList<MutableList<Any>> = mutableListOf()

        for (i in fullSymbolField.indices) {
            val row: MutableList<Any> = mutableListOf()

            for (j in fullSymbolField[i].indices) {
                val element = fullSymbolField[i][j]

                if (element == "/" || element == "*" || isIndexValid(i - 1, j, fullSymbolField.size, fullSymbolField[i].size) && fullSymbolField[i - 1][j] == "/" ||
                    isIndexValid(i + 1, j, fullSymbolField.size, fullSymbolField[i].size) && fullSymbolField[i + 1][j] == "/" ||
                    isIndexValid(i, j - 1, fullSymbolField.size, fullSymbolField[i].size) && fullSymbolField[i][j - 1] == "/" ||
                    isIndexValid(i, j + 1, fullSymbolField.size, fullSymbolField[i].size) && fullSymbolField[i][j + 1] == "/"
                ) {
                    row.add(element)
                } else {
                    row.add(".")
                }
            }

            lista2.add(row)
        }
        finalField.clear()
        finalField = lista2
        for (i in setList) {
            if (finalField[i.first][i.second] == "." || finalField[i.first][i.second] == "*") {
                finalField[i.first][i.second] = "*"
            } else setList.remove(i.first to i.second)
        }
        for (i in lostNumList) finalField[i.first][i.second] = fullSymbolField[i.first][i.second]

       if (fullSymbolField.flatten().count { it == "." } == 0 && finalField.flatten().count { it == "." } == minesLocation.size) {
           printField(finalField)
           println("Congratulations! You found all the mines!")
           exitProcess(0)
       }

        printField(finalField)





    }

     */

    private fun finalMerged() {
        fun isIndexValid(rowIndex: Int, colIndex: Int, rows: Int, cols: Int): Boolean {
            return rowIndex in 0 until rows && colIndex in 0 until cols
        }

        fun fillDiagonal(rowIndex: Int, colIndex: Int, rows: Int, cols: Int) {
            if (isIndexValid(rowIndex, colIndex, rows, cols) && finalField[rowIndex][colIndex] == ".") {
                finalField[rowIndex][colIndex] = "/"
                fillDiagonal(rowIndex - 1, colIndex - 1, rows, cols)
                fillDiagonal(rowIndex - 1, colIndex + 1, rows, cols)
                fillDiagonal(rowIndex + 1, colIndex - 1, rows, cols)
                fillDiagonal(rowIndex + 1, colIndex + 1, rows, cols)
            }
        }

        val mergedField: MutableList<MutableList<Any>> = mutableListOf()

        for (i in fullSymbolField.indices) {
            val row: MutableList<Any> = mutableListOf()

            for (j in fullSymbolField[i].indices) {
                val element = fullSymbolField[i][j]

                if (element == "/" || element == "*" ||
                    isIndexValid(i - 1, j, fullSymbolField.size, fullSymbolField[i].size) && fullSymbolField[i - 1][j] == "/" ||
                    isIndexValid(i + 1, j, fullSymbolField.size, fullSymbolField[i].size) && fullSymbolField[i + 1][j] == "/" ||
                    isIndexValid(i, j - 1, fullSymbolField.size, fullSymbolField[i].size) && fullSymbolField[i][j - 1] == "/" ||
                    isIndexValid(i, j + 1, fullSymbolField.size, fullSymbolField[i].size) && fullSymbolField[i][j + 1] == "/" ||
                    isIndexValid(i - 1, j - 1, fullSymbolField.size, fullSymbolField[i].size) && fullSymbolField[i - 1][j - 1] == "/" ||
                    isIndexValid(i - 1, j + 1, fullSymbolField.size, fullSymbolField[i].size) && fullSymbolField[i - 1][j + 1] == "/" ||
                    isIndexValid(i + 1, j - 1, fullSymbolField.size, fullSymbolField[i].size) && fullSymbolField[i + 1][j - 1] == "/" ||
                    isIndexValid(i + 1, j + 1, fullSymbolField.size, fullSymbolField[i].size) && fullSymbolField[i + 1][j + 1] == "/"
                ) {
                    row.add(element)
                } else if ((isIndexValid(i - 1, j - 1, fullSymbolField.size, fullSymbolField[i].size) && fullSymbolField[i - 1][j - 1] == "/") &&
                    ((isIndexValid(i - 1, j, fullSymbolField.size, fullSymbolField[i].size) && fullSymbolField[i - 1][j] != ".") ||
                            (isIndexValid(i, j - 1, fullSymbolField.size, fullSymbolField[i].size) && fullSymbolField[i][j - 1] != ".") ||
                            (isIndexValid(i - 1, j + 1, fullSymbolField.size, fullSymbolField[i].size) && fullSymbolField[i - 1][j + 1] != ".") ||
                            (isIndexValid(i + 1, j - 1, fullSymbolField.size, fullSymbolField[i].size) && fullSymbolField[i + 1][j - 1] != "."))
                ) {
                    row.add(fullSymbolField[i][j - 1])
                } else {
                    row.add(".")
                }

                // Wypełnienie po przekątnych
                fillDiagonal(i - 1, j - 1, fullSymbolField.size, fullSymbolField[i].size)
                fillDiagonal(i - 1, j + 1, fullSymbolField.size, fullSymbolField[i].size)
                fillDiagonal(i + 1, j - 1, fullSymbolField.size, fullSymbolField[i].size)
                fillDiagonal(i + 1, j + 1, fullSymbolField.size, fullSymbolField[i].size)
            }

            mergedField.add(row)
        }

        finalField.clear()
        finalField = mergedField

        for (i in setList.toList()) {
            if (finalField[i.first][i.second] == "." || finalField[i.first][i.second] == "*") {
                finalField[i.first][i.second] = "*"
            } else {
                setList.remove(i)
            }
        }

        for (i in lostNumList) {
            finalField[i.first][i.second] = fullSymbolField[i.first][i.second]
        }

        if (fullSymbolField.flatten().count { it == "." } == minesLocation.size && finalField.flatten().count { it == "*" } == minesLocation.size) {
            printField(finalField)
            println("Congratulations! You found all the mines!")
        }

        printField(finalField)
    }






    private fun lose(){
        for (i in minesLocation) {
            finalField[i.first][i.second] = "X"
        }
        printField(finalField)
        println("You stepped on a mine and failed!")
        exitProcess(0)

    }
}
/*
for (i in setList) {
    if (finalField[i.first][i.second] == "." || finalField[i.first][i.second] == "*") {
        finalField[i.first][i.second] = "*"
    } else setList.remove(i.first to i.second)
}

 */
    






