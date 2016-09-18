package com.r3corda.contracts.universal

import com.r3corda.core.contracts.Frequency
import java.math.BigDecimal

/**
 * Created by sofusmortensen on 28/06/16.
 */

// Swaption


class Swaption {

    val notional = 10.M
    val currency = USD
    val coupon = 1.5.bd

    val dreary_contract = arrange {
        actions {
            (highStreetBank or acmeCorp).may {
                "proceed".givenThat(after("01/07/2015")) {
                    highStreetBank.gives(acmeCorp, libor(notional, "01/04/2015", "01/07/2015"), currency)
                    acmeCorp.gives(highStreetBank, interest(notional, "act/365", coupon, "01/04/2015", "01/07/2015"), currency)
                    actions {
                        (highStreetBank or acmeCorp).may {
                            "proceed".givenThat(after("01/10/2015")) {
                                highStreetBank.gives(acmeCorp, libor(notional, "01/07/2015", "01/10/2015"), currency)
                                acmeCorp.gives(highStreetBank, interest(notional, "act/365", coupon, "01/07/2015", "01/10/2015"), currency)

                                actions {
                                    (highStreetBank or acmeCorp).may {
                                        "dummy".anytime { zero }
                                        // etc ...
                                    }
                                }
                            }
                        }
                    }
                    actions {
                        acmeCorp.may {
                            "cancel".anytime {
                                acmeCorp.gives(highStreetBank, 10.K, USD)
                            }
                        }
                    }
                }
            }
            acmeCorp.may {
                "cancel".anytime {
                    acmeCorp.gives(highStreetBank, 10.K, USD)
                }
            }
        }
    }


    val elegant_contract = arrange {
        rollOut("01/04/2015".ld, "01/04/2025".ld, Frequency.Quarterly) {
            actions {
                (highStreetBank or acmeCorp).may {
                    "proceed".givenThat(after(start)) {
                        highStreetBank.gives(acmeCorp, libor(notional, start, end), currency)
                        acmeCorp.gives(highStreetBank, interest(notional, "act/365", coupon, start, end), currency)
                        next()
                    }
                }
                acmeCorp.may {
                    "cancel".anytime {
                        acmeCorp.gives(highStreetBank, 10.K, currency)
                    }
                }
            }
        }
    }

    val strike = 1.2

    val tarf = arrange {
        rollOut("01/04/2015".ld, "01/04/2016".ld, Frequency.Quarterly, object {
            val cap = variable(150.K)
        }) {
            actions {
                acmeCorp.may {
                    "exercise".givenThat(before(end)) {
                        val payout = (EUR / USD - strike).plus() * notional

                        actions {
                            (acmeCorp or highStreetBank).may {
                                "proceed".givenThat(after(end)) {
                                    highStreetBank.gives(acmeCorp, payout, USD)
                                    next(vars.cap to vars.cap - payout)
                                }
                            }
                        }
                    }
                }
                (acmeCorp or highStreetBank).may {
                    "proceedWithoutExercise".givenThat(after(end)) {
                        next()
                    }
                }
            }
        }
    }

    val tarf2 = arrange {
        rollOut("01/04/2015".ld, "01/04/2016".ld, Frequency.Quarterly, object {
            val uses = variable(4)
        }) {
            actions {
                acmeCorp.may {
                    "exercise".givenThat(before(end)) {
                        val payout = (EUR / USD - strike).plus() * notional

                        actions {
                            (acmeCorp or highStreetBank).may {
                                "proceed".givenThat(after(end)) {
                                    highStreetBank.gives(acmeCorp, payout, currency)
                                    next(vars.uses to vars.uses - 1)
                                }
                            }
                        }
                    }
                }
                (acmeCorp or highStreetBank).may {
                    "proceedWithoutExercise".givenThat(after(end)) {
                        next()
                    }
                }
            }
        }
    }
}