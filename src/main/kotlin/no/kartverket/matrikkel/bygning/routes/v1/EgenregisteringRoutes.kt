package no.kartverket.matrikkel.bygning.routes.v1

import io.bkbn.kompendium.core.metadata.PostInfo
import io.bkbn.kompendium.core.plugin.NotarizedRoute
import io.bkbn.kompendium.json.schema.definition.TypeDefinition
import io.bkbn.kompendium.oas.payload.Parameter
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import no.kartverket.matrikkel.bygning.models.Egenregistrering
import no.kartverket.matrikkel.bygning.services.EgenregistreringsService

fun Route.egenregistreringRouting(egenregistreringsService: EgenregistreringsService) {
    route("/egenregistrering") {
        route("{bygningId}") {
            egenregistreringBygningIdDoc()
            post {
                val egenregistrering = call.receive<Egenregistrering>()


                if (egenregistreringsService.addEgenregistreringToBygning(egenregistrering)) {
                    call.respondText(
                        "Egenregistrering registrert på bygning ${egenregistrering.bygningsRegistrering.bygningsId}",
                        status = HttpStatusCode.OK
                    )
                } else {
                    call.respondText(
                        "Det ble forsøkt registrert egenregistreringer på bruksenheter som ikke har tilknytning til bygningen",
                        status = HttpStatusCode.BadRequest
                    )
                }
            }
        }
    }
}

private fun Route.egenregistreringBygningIdDoc() {
    install(NotarizedRoute()) {
        parameters = listOf(
            Parameter(
                name = "id", `in` = Parameter.Location.path, schema = TypeDefinition.STRING
            )
        )
        post = PostInfo.builder {
            summary("Legg til en egenregistrering på en bygning")
            description("Legger til en egenregistrering på en bygning og tilhørende bruksenheter, hvis noen")
            request {
                requestType<Egenregistrering>()
                required(true)
                description("Egeneregistrert data")
            }
            response {
                responseCode(HttpStatusCode.OK)
                responseType<String>()
                description("Bygninger og eventuelle bruksenheter registrert")
            }

            canRespond {
                responseCode(HttpStatusCode.BadRequest)
                responseType<String>()
                description("Alle bruksenheter som kom i request tilhørte ikke bygningen")
            }
        }
    }
}