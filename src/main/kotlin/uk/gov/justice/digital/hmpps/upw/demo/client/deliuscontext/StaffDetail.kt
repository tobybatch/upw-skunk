package uk.gov.justice.digital.hmpps.upw.demo.client.deliuscontext

data class StaffDetail(
  val email: String?,
  val telephoneNumber: String?,
  val teams: List<Team> = emptyList(),
  val probationArea: ProbationArea,
  val username: String?,
  val name: PersonName,
  val code: String,
  val active: Boolean,
) {
  fun activeTeamsNewestFirst() = teams
    .filter { it.endDate == null }
    .sortedByDescending { it.startDate }
}

data class PersonName(
  val forename: String,
  val surname: String,
  val middleName: String? = null,
) {
  fun deliusName() = forenames() + " $surname"
  fun forenames() = "$forename ${middleName?.takeIf { it.isNotEmpty() } ?: ""}".trim()
}

data class ProbationArea(
  val code: String,
  val description: String,
)
