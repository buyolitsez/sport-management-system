package testsForPrintStandingsByResults

import basicClasses.*
import classes.AllCheckpointsCalculator
import standings.StandingsInGroups
import writers.GroupStandingsPrinter
import java.io.File
import kotlin.test.Test
import kotlin.test.assertContentEquals

internal class TestPrintStandingsByGroups {
    private fun generateCompetition(): Competition {
        val teams = listOf(
            Team(
                "ПСКОВ,РУСЬ", mutableListOf(
                    Competitor("VIP", "НИКИТИН", "ВАЛЕНТИН", "1941", "", "", "", 1),
                    Competitor("VIP", "НИКИТИНА", "АЛЛА", "1939", "КМС", "", "", 2),
                    Competitor("VIP", "ТИХОМИРОВ", "ИВАН", "2007", "", "", "", 3),
                    Competitor("М14", "ЖЕЛЕЗНЫЙ", "МИХАИЛ", "2007", "", "", "", 4)
                )
            ), Team(
                "ПИТЕР",
                mutableListOf(Competitor("VIP", "ПУПКИН", "ВАСЯ", "2013", "КМС", "", "", 5))
            ), Team(
                "МОСКВА",
                mutableListOf(Competitor("М14", "ПУПКИН", "ВАСЯ", "2013", "КМС", "", "", 6))
            )
        )

        val groups = listOf("VIP", "М14").associateWith { Group(it, listOf("start", "finish"), AllCheckpointsCalculator) }

        var acc = 0
        val competitors = teams.map { team ->
            team.competitors.map {
                acc++
                CompetitorInCompetition(
                    it,
                    acc.toString(),
                    groups.getOrDefault(it.wishGroup, Group(it.wishGroup, listOf("start", "finish"), AllCheckpointsCalculator)),
                    team
                )
            }
        }.flatten()

        val startTimes = listOf(
            Time("12:00:00"),
            Time("13:00:00"),
            Time("14:00:00"),
            Time("15:00:00"),
            Time("16:00:00"),
            Time("17:00:00")
        )
        val finishTimes = listOf(
            Time("12:32:56"),
            Time("13:33:46"),
            Time("14:33:46"),
            Time("15:34:49"),
            Time("16:35:54"),
        )

        val checkPoints = listOf(
            CheckPoint(
                "start",
                competitors.associateWith { mutableListOf(startTimes[it.number.toInt() - 1]) }.toMutableMap()
            ),
            CheckPoint(
                "finish",
                competitors.dropLast(1).associateWith { mutableListOf(finishTimes[it.number.toInt() - 1]) }
                    .toMutableMap()
            )
        )

        return Competition(
            checkPoints.toMutableList(),
            competitors,
            listOf("1", "2", "3", "4", "5", "6").associateWith { competitors[it.toInt() - 1] }
        )
    }

    @Test
    fun testSimpleGroups() {
        val competition = generateCompetition()
        File("./testData/testPrintStandingsByGroups/standingsSimpleGroup.csv").bufferedWriter().use { print("") }
        GroupStandingsPrinter(
            "./testData/testPrintStandingsByGroups/standingsSimpleGroup.csv"
        ).print(StandingsInGroups(competition))

        assertContentEquals(
            File("./testData/testPrintStandingsByGroups/expectedStandingsSimpleGroup.csv").readLines(),
            File("./testData/testPrintStandingsByGroups/standingsSimpleGroup.csv").readLines()
        )
    }

    @Test
    fun testEmptyCompetition() {
        val competition = Competition(
            mutableListOf(CheckPoint("start", mutableMapOf()), CheckPoint("finish", mutableMapOf())),
            listOf(),
            mapOf()
        )

        File("./testData/testPrintStandingsByGroups/standingsEmptyGroup.csv").bufferedWriter().use { print("") }
        GroupStandingsPrinter(
            "./testData/testPrintStandingsByGroups/standingsEmptyGroup.csv"
        ).print(StandingsInGroups(competition))

        assertContentEquals(
            File("./testData/testPrintStandingsByGroups/expectedStandingsEmptyGroup.csv").readLines(),
            File("./testData/testPrintStandingsByGroups/standingsEmptyGroup.csv").readLines()
        )
    }
}