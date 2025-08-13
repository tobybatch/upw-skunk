package uk.gov.justice.digital.hmpps.upw.demo.client.deliuscontext

data class UserOffenderAccess(
  val userRestricted: Boolean,
  val userExcluded: Boolean,
  val restrictionMessage: String?,
)
