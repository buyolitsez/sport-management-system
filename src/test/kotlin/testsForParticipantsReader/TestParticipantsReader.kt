package testsForParticipantsReader

import basicClasses.IncompleteCheckPointRecord
import basicClasses.IncompleteCheckpoint
import basicClasses.IncompleteCompetition
import basicClasses.Time
import org.junit.Test
import readers.DirectoryReaderException
import readers.ParticipantsResultsReader
import readers.ParticipantsResultsReaderExceptionSameNumber
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

internal class TestParticipantsReader {
    private val dirPath =
        "testData/testDataFolderReaderResultForEachParticipant/testReadListOfIncompleteCheckpointsFromDirectoryWithParticipantsResults/"

    @Test
    fun testThreeParticipants() {
        val listOfCheckpoints = ParticipantsResultsReader(
            dirPath + "testThreeParticipants/", listOf("1km", "2km", "finish")
        ).read()
        val competition = IncompleteCompetition(
            listOf(
                IncompleteCheckpoint(
                    "1km", listOf(
                        IncompleteCheckPointRecord("first", Time("12:00:00")),
                        IncompleteCheckPointRecord("third", Time("12:01:06")),
                    )
                ),
                IncompleteCheckpoint(
                    "2km", listOf(
                        IncompleteCheckPointRecord("first", Time("13:05:06")),
                        IncompleteCheckPointRecord("second", Time("00:00:00")),
                    )
                ),
                IncompleteCheckpoint(
                    "finish", listOf()
                )
            )
        )

        assertEquals(
            competition.checkpoints, listOfCheckpoints.checkpoints
        )
    }

    @Test
    fun testDirectoryDoesntExist() {
        assertFailsWith<DirectoryReaderException> {
            ParticipantsResultsReader(
                "",
                listOf("1km", "2km")
            ).read()
        }
    }

    @Test
    fun testEmptyDirectory() {
        val listOfCheckpoints = ParticipantsResultsReader(
            dirPath + "testEmptyDirectory/",
            listOf("1km", "2km", "finish")
        ).read()
        val competition = IncompleteCompetition(
            listOf(
                IncompleteCheckpoint("1km", listOf()),
                IncompleteCheckpoint("2km", listOf()),
                IncompleteCheckpoint("finish", listOf()),
            )
        )
        assertEquals(
            competition.checkpoints, listOfCheckpoints.checkpoints
        )
    }


    @Test
    fun testRepeatedParticipants() {
        assertFailsWith<ParticipantsResultsReaderExceptionSameNumber> {
            ParticipantsResultsReader(
                dirPath + "testRepeatedParticipants/",
                listOf("1km", "2km", "Finish")
            ).read()
        }
    }
}