import classes.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.lang.Exception

interface CompetitorsDB {
    /**
     * data keys format:
     * id, wishGroup, surname, team, name, birth, title
     */
    fun updateCompetitor(id: Int, data: Map<String, String>)
    fun createEmptyCompetitor(): Int
    fun getAllCompetitors(columns: List<String>): List<List<String>>
    fun cleanCompetitors()
    fun deleteCompetitor(id: Int)
}

interface SortitionDB {
    fun setCompetition(competition: Competition)
    fun getCompetition(): Competition
}

class DB(name: String) : CompetitorsDB, SortitionDB {
    companion object {
        const val idPropertyName = "id"
        const val wishGroupPropertyName = "wishGroup"
        const val surnamePropertyName = "surname"
        const val namePropertyName = "name"
        const val titlePropertyName = "title"
        const val birthPropertyName = "birth"
        const val teamPropertyName = "team"
    }

    object Competitors : Table("Applications") {
        val id = integer("id").autoIncrement().primaryKey()
        val wishGroup = text("wishGroup").nullable()
        val surname = text("surname").nullable()
        val team = text("team").nullable()
        val name = text("name").nullable()
        val birth = text("birth").nullable()
        val title = text("title").nullable()
    }

    object CompetitorsInCompetition : Table("CompetitorsInCompetition") {
        val id = reference("id", Competitors.id).uniqueIndex()
        val number = text("number")
        val group = text("group")
        val startTime = integer("startTime")
        val groupCalculatorId = integer("groupCalculatorId")
        val checkpointNames = text("checkpointNames")
    }

    private val db = Database.connect("jdbc:h2:./$name;DB_CLOSE_DELAY=-1;", "org.h2.Driver")

    init {
        transaction(db) {
            SchemaUtils.create(Competitors, CompetitorsInCompetition)
        }
    }

    override fun updateCompetitor(id: Int, data: Map<String, String>) {
        transaction {
            data.forEach { (property, value) ->
                Competitors.update({ Competitors.id eq id }) {
                    when (property) {
                        wishGroupPropertyName -> it[wishGroup] = value
                        surnamePropertyName -> it[surname] = value
                        namePropertyName -> it[name] = value
                        titlePropertyName -> it[title] = value
                        birthPropertyName -> it[birth] = value
                        teamPropertyName -> it[team] = value
                        else -> throw Exception("Unknown property!")
                    }
                }
            }
        }
    }

    override fun createEmptyCompetitor(): Int {
        return transaction(db) {
            addLogger(StdOutSqlLogger)
            SchemaUtils.create(Competitors)
            Competitors.insert {
                it[wishGroup] = null
                it[surname] = null
                it[name] = null
                it[title] = null
                it[birth] = null
                it[team] = null
            } get Competitors.id
        }
    }

    override fun getAllCompetitors(columns: List<String>): List<List<String>> {
        return transaction(db) {
            Competitors.selectAll().map {
                columns.map { column ->
                    when (column) {
                        idPropertyName -> it[Competitors.id].toString()
                        wishGroupPropertyName -> it[Competitors.wishGroup] ?: ""
                        surnamePropertyName -> it[Competitors.surname] ?: ""
                        namePropertyName -> it[Competitors.name] ?: ""
                        titlePropertyName -> it[Competitors.title] ?: ""
                        birthPropertyName -> it[Competitors.birth] ?: ""
                        teamPropertyName -> it[Competitors.team] ?: ""
                        else -> throw Exception("Unknown property!")
                    }
                }
            }
        }
    }

    override fun cleanCompetitors() {
        transaction(db) {
            SchemaUtils.drop(Competitors)
            SchemaUtils.create(Competitors)
        }
    }

    override fun deleteCompetitor(id: Int) {
        transaction(db) {
            Competitors.deleteWhere { Competitors.id eq id }
        }
    }

    override fun setCompetition(competition: Competition) {
        transaction(db) {
            competition.competitors.forEach { competitor ->
                CompetitorsInCompetition.insert {
                    it[id] = competitor.id
                    it[startTime] = competition.start.timeMatching[competitor]?.first()?.time
                        ?: throw Exception("I don't know what happened :( Everything must be ok!")
                    it[number] = competitor.number
                    it[group] = competitor.group.name
                    it[groupCalculatorId] =
                        when (competitor.group.calculator) {
                            is AllCheckpointsCalculator -> 0
                            is KCheckpointsCalculator -> competitor.group.calculator.minCheckpoints
                            else -> throw Exception("I don't know such calculator :(")
                        }
                    it[checkpointNames] = competitor.group.checkPointNames.joinToString("$")
                }
                Competitors.update({ Competitors.id eq competitor.id }) {
                    it[team] = competitor.team.name
                }
            }
        }
    }

    override fun getCompetition(): Competition {
        return transaction(db) {

            val groups = (CompetitorsInCompetition innerJoin Competitors)
                .selectAll().map {
                    Triple(
                        it[CompetitorsInCompetition.group],
                        it[CompetitorsInCompetition.checkpointNames].split("$"),
                        when (it[CompetitorsInCompetition.groupCalculatorId]) {
                            0 -> AllCheckpointsCalculator
                            else -> KCheckpointsCalculator(it[CompetitorsInCompetition.groupCalculatorId])
                        }
                    )
                }.distinct().map { Group(it.first, it.second, it.third) }

            val competitors = (CompetitorsInCompetition innerJoin Competitors).selectAll().map {
                Competitor(
                    it[Competitors.wishGroup]
                        ?: throw Exception("I don't know what happened :( Everything must be ok!"),
                    it[Competitors.surname]
                        ?: throw Exception("I don't know what happened :( Everything must be ok!"),
                    it[Competitors.name]
                        ?: throw Exception("I don't know what happened :( Everything must be ok!"),
                    it[Competitors.birth]
                        ?: throw Exception("I don't know what happened :( Everything must be ok!"),
                    it[Competitors.title]
                        ?: throw Exception("I don't know what happened :( Everything must be ok!"),
                    "", "",
                    it[Competitors.id]
                )
            }


            val teams = (CompetitorsInCompetition innerJoin Competitors)
                .selectAll().groupBy(
                    { it[Competitors.team]!! }, {
                        competitors.find { competitor -> competitor.id == it[Competitors.id] }!!
                    }
                ).map { (teamName, comps) ->
                    Team(teamName, comps)
                }


            val compInComp = (CompetitorsInCompetition innerJoin Competitors).selectAll().map {
                CompetitorInCompetition(
                    competitors.find { competitor -> competitor.id == it[Competitors.id] }!!,
                    it[CompetitorsInCompetition.number],
                    groups.find { group -> group.name == it[CompetitorsInCompetition.group] }!!,
                    teams.find { team -> team.name == it[Competitors.team] }!!
                )
            }

            val numberMatching = compInComp.associateBy { competitor -> competitor.number }


            val timeMatching = (CompetitorsInCompetition).selectAll().groupBy(
                {
                    compInComp.find { comp -> it[CompetitorsInCompetition.id] == comp.id }!!
                }, {
                    Time(it[CompetitorsInCompetition.startTime])
                }
            )

            Competition(
                checkpoints = mutableListOf(CheckPoint("", timeMatching)),
                competitors = compInComp,
                numberMatching = numberMatching
            )
        }
    }
}