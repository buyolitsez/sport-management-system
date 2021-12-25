package gui

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

class Table<T>(private var data: MutableList<T>, private val default: T) {
    fun add() {
        data.add(default)
    }

    fun delete(indices: Set<Int>) {
        data = data.filterIndexed { index, t -> index !in indices }.toMutableList()
    }

    val size: Int
        get() = data.size

    @Composable
    fun draw(stateVertical: ScrollState) {
        Column(modifier = Modifier.verticalScroll(stateVertical)) {
            data.forEach {
                TextField(
                    it.toString(),
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier.height(60.dp)
                )
            }
        }
    }
}

/*class Table(
    private var columnNames: List<String>,
    private var tableData: List<List<String>>,
    private val db: CompetitorsDB
) {

    @Composable
    fun drawTable(
        offsetX: Dp,
        offsetY: Dp,
        showDelete: Boolean,
        selected: MutableSet<Int>,
        columnToSort: MutableState<Int>,
        lastButton: MutableState<MyButtons>,
        isMutable: Boolean
    ) {
        val realData = tableData.map { it.toMutableList() }.toMutableList()
        logger.debug { "drawing table ($columnNames, $realData)" }
        val backgroundColor = Color.LightGray
        val countOfColumns = columnNames.size

        val buttonWidth = 30.dp

        val rowSize = remember { mutableStateOf(IntSize.Zero) }

        realData.forEach { require(it.size == countOfColumns) { "Wrong count of columns in table data($columnNames\n $realData)" } }
        require(columnNames[0] == "id") { "first column is not an id" }

        LazyColumn(Modifier.fillMaxWidth().padding(16.dp).offset(x = offsetX, y = offsetY).onSizeChanged {
            rowSize.value = it
        }) {
            item {
                Row(Modifier.fillMaxWidth().background(backgroundColor)) {
                    columnNames.indices.forEach { index ->
                        Button(
                            onClick = {
                                columnToSort.value = index
                                lastButton.value = MyButtons.SORTED_BY_COLUMN
                            },
                            modifier = Modifier.background(Color.LightGray)
                                .width((rowSize.value.width / countOfColumns).dp)
                        ) {
                            Text(text = columnNames[index])
                        }
                    }
                }
            }

            for (i in realData.indices) {
                item {
                    Row(Modifier.background(backgroundColor)) {
                        if (showDelete) {
                            Button(
                                modifier = Modifier.width(buttonWidth),
                                colors = ButtonDefaults.buttonColors(backgroundColor = if (realData[i][0].toInt() in selected) Color.Red else Color.Gray),
                                onClick = {
                                    val id = realData[i][0].toInt()
                                    if (id in selected)
                                        selected.remove(id)
                                    else
                                        selected.add(id)
                                    lastButton.value = MyButtons.SELECT_ROW
                                }) {
                                Text("X")
                            }
                        }

                        for (j in realData[i].indices) {
                            val curText = remember { mutableStateOf(realData[i][j]) }

                            TextField(
                                value = curText.value,
                                onValueChange = {
                                    curText.value = it
                                    realData[i][j] = it

                                    db.updateCompetitor(
                                        realData[i][0].toInt(),
                                        columnNames.drop(1).zip(realData[i].drop(1)).toMap()
                                    )
                                }, readOnly = (j == 0) || !isMutable,
                                modifier = Modifier.width((rowSize.value.width / countOfColumns).dp)
                            )
                        }
                    }
                }
            }
        }
    }
}*/
