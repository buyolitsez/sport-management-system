package gui

import GUIController
import GUIViewer
import classes.*

//data class CompetitorWithTeam(val team: Team, val competitor: Competitor)

data class CheckPointWithCount(val checkPoint: CheckPoint, val index: Int)

object Tables {
    /*fun applicationsTable() = Table<CompetitorWithTeam>(
        columns = listOf(
            Column("team", false, { team.name }, {}),
            Column("surname", false, { competitor.surname }, {}),
            Column("name", false, { competitor.name }, {}),
            Column("birth", false, { competitor.birth }, {}),
            Column("title", false, { competitor.title }, {}),
            Column("medical examination", false, { competitor.medicalExamination }, {}),
            Column("medical insurance", false, { competitor.medicalInsurance }, {})
        ),
        tableData = GUI.database.getAllApplications().teams.map {
            it.competitors.map { competitor -> CompetitorWithTeam(it, competitor) }
        }.flatten().toMutableList()
    )*/

    fun teamTable(team: Team, controller: GUIController, viewer: GUIViewer) = MutableTable<Competitor>(
        columns = listOf(
            Column("surname", true, { surname }, {}),
            Column("name", true, { name }, {}),
            Column("birth", true, { birth }, {}),
            Column("title", true, { title }, {}),
            Column("medical examination", true, { medicalExamination }, {}),
            Column("medical insurance", true, { medicalInsurance }, {})
        ),
        tableData = team.competitors.toMutableList(),
        delete = {
            team.competitors.remove(it)
            controller.updateApplications()
        },
        add = {
            val competitor = Competitor("", "", "", "", "", "", "")
            team.competitors.add(competitor)
            controller.updateApplications()
            competitor
        }
    )

    fun groupTable(group: Group, controller: GUIController, viewer: GUIViewer) = Table<CompetitorInCompetition>(
        columns = listOf(
            Column("number", false, { number }, {}),
            Column("team", false, { team.name }, {}),
            Column("surname", false, { surname }, {}),
            Column("name", false, { name }, {}),
            Column("birth", false, { birth }, {}),
            Column("title", false, { title }, {}),
            Column("medical examination", false, { medicalExamination }, {}),
            Column("medical insurance", false, { medicalInsurance }, {}),
            Column(
                "time",
                false,
                { viewer.competition?.start?.timeMatching?.get(this)?.first()?.stringRepresentation ?: "" },
                {})
        ),
        tableData = (viewer.competition?.competitors ?: listOf()).toMutableList(),
    )

    private fun columnsByCheckpoints(
        group: Group,
        controller: GUIController,
        viewer: GUIViewer
    ): List<Column<CompetitorInCompetition>> {
        val checkpoints =
            group.checkPointNames.mapNotNull { name -> viewer.competition?.checkpoints?.find { it.name == name } }
        val withCount = checkpoints.mapIndexed { index, checkPoint ->
            CheckPointWithCount(checkPoint, checkpoints.take(index).count { it.name == checkPoint.name })
        }
        return withCount.map { checkpoint ->
            Column(checkpoint.checkPoint.name, true,
                { checkpoint.checkPoint.timeMatching[this]?.get(checkpoint.index)?.stringRepresentation ?: "" },
                {
                    checkpoint.checkPoint.timeMatching[this]?.set(checkpoint.index, Time(it))
                    controller.updateResults()
                }
            )
        }
    }

    fun checkpointsTable(group: Group, controller: GUIController, viewer: GUIViewer) = Table<CompetitorInCompetition>(
        columns = listOf(Column<CompetitorInCompetition>("name", false, { name }, {}))
                + columnsByCheckpoints(group, controller, viewer),
        tableData = (viewer.competition?.competitors?.filter { it.group == group } ?: listOf()).toMutableList(),
    )
}