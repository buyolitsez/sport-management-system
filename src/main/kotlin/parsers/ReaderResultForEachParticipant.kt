package parsers

import classes.Time
import com.github.doyaaaaaken.kotlincsv.client.CsvFileReader
import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import logger

data class CheckpointsForParticipant(val personNumber: String, val timeMatching: Map<String, Time>)

private fun CsvFileReaderWithFileName.getPersonNumberFromRecord(record: List<String>?): String =
    getFirstFieldFromRecord(record)

private fun convertRecordsToTimeMatching(list: List<List<String>>): Map<String, Time> {
    val timeMatching = mutableMapOf<String, Time>()
    for (record in list) {
        require(record.size == 2) { "Record($record) size is not 2" }
        timeMatching[record[0]] = Time(record[1])
    }
    return timeMatching
}

private fun CsvFileReaderWithFileName.createCheckpointsFromFile(): CheckpointsForParticipant {
    val personNumber = getPersonNumberFromRecord(readSingleRecord())
    val checkpoints = convertRecordsToTimeMatching(readAllRemainingRecords())
    logger.debug { "Person number = $personNumber, checkpoints = $checkpoints" }
    return CheckpointsForParticipant(personNumber, checkpoints)
}

fun readCheckpointsFromOneParticipant(fileName: String): CheckpointsForParticipant {
    val createCheckpoint = { csvFileReader: CsvFileReader ->
        CsvFileReaderWithFileName(fileName, csvFileReader).createCheckpointsFromFile()
    }
    val checkpointsForParticipant = csvReader().open(fileName, createCheckpoint)
    logger.debug { "CheckPoint($checkpointsForParticipant) from file($fileName)" }
    return checkpointsForParticipant
}

data class IncompleteCheckpoint(val name: String, val timeMatching: Map<String, Time>)

fun readListOfCheckpointsFromDirectoryWithParticipantsResults(
    dir: String,
    checkpointNames: List<String>
): List<IncompleteCheckpoint> {
    val countOfCheckpoints = checkpointNames.size

    val checkPointsForParticipant = getMappedListOfFilesFromDir(dir, ::readCheckpointsFromOneParticipant)

    val checkpoints = List(countOfCheckpoints) { index ->
        val currentCheckpointName = checkpointNames[index]
        val timeMatching = mutableMapOf<String, Time>()
        checkPointsForParticipant.forEach { participant ->
            participant.timeMatching[currentCheckpointName]?.let { time ->
                timeMatching[participant.personNumber] = time
            }
        }
        IncompleteCheckpoint(currentCheckpointName, timeMatching)
    }
    return checkpoints
}