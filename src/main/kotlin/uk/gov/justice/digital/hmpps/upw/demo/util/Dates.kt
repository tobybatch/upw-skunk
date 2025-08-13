package uk.gov.justice.digital.hmpps.upw.demo.util

import io.netty.util.internal.ThreadLocalRandom
import java.time.Instant

fun Instant.minusRandomSeconds(maxOffset: Long): Instant {
    val randomOffset = ThreadLocalRandom
        .current()
        .nextLong(-maxOffset, 0)

    return this.plusSeconds(randomOffset)
}