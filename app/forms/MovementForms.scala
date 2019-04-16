/*
 * Copyright 2019 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package forms

import java.time.LocalDateTime

import forms.Choice.AllowedChoiceValues
import play.api.data.Form
import play.api.data.Forms.{mapping, optional, text}
import play.api.libs.json.Json
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.wco.dec.inventorylinking.common.{AgentDetails, TransportDetails, UcrBlock}
import uk.gov.hmrc.wco.dec.inventorylinking.movement.request.InventoryLinkingMovementRequest

case class GoodsDateForm(day: String, month: String, year: String, hour: Option[String], minute: Option[String])

object GoodsDateForm {
  implicit val format = Json.format[GoodsDateForm]

  val days = (1 to 31).toList.map(_.toString)
  val months = (1 to 12).toList.map(_.toString)
  val hours = (0 to 23).toList.map(_.toString)
  val minutes = (0 to 59).toList.map(_.toString)

  //scalastyle:off magic.number
  val goodsDateMapping = mapping(
    "day" -> text().verifying("Day is incorrect", days.contains(_)),
    "month" -> text().verifying("Month is incorrect", months.contains(_)),
    "year" -> text()
      .verifying(
        "Year is incorrect",
        year =>
          if (year.isEmpty) false
          else year.toInt >= LocalDateTime.now().getYear
      ),
    "hour" -> optional(text().verifying("Hour is incorrect", hours.contains(_))),
    "minute" -> optional(text().verifying("Minutes are incorrect", minutes.contains(_)))
  )(GoodsDateForm.apply)(GoodsDateForm.unapply)
  //scalastyle:on magic.number
}

case class LocationForm(
  agentLocation: Option[String],
  agentRole: Option[String],
  goodsLocation: Option[String],
  shed: Option[String]
)

object LocationForm {
  implicit val format = Json.format[LocationForm]

  val locationMapping = mapping(
    "agentLocation" -> optional(text()),
    "agentRole" -> optional(text()),
    "goodsLocation" -> optional(text()),
    "shed" -> optional(text())
  )(LocationForm.apply)(LocationForm.unapply)
}

case class TransportForm(
  transportId: Option[String],
  transportMode: Option[String],
  transportNationality: Option[String]
)

object TransportForm {
  implicit val format = Json.format[TransportForm]

  val transportMapping =
    mapping(
      "transportId" -> optional(text(maxLength = 35)),
      "transportMode" -> optional(text(maxLength = 1)),
      "transportNationality" -> optional(text(maxLength = 2))
    )(TransportForm.apply)(TransportForm.unapply)
}

object MovementFormsAndIds {
  val goodsDateForm = Form(GoodsDateForm.goodsDateMapping)
  val goodsDateId = "GoodsDate"

  val locationForm = Form(LocationForm.locationMapping)
  val locationId = "Location"

  val transportForm = Form(TransportForm.transportMapping)
  val transportId = "Transport"
}

object Movement {

  def createMovementRequest(cacheMap: CacheMap, eori: String, choice: Choice): InventoryLinkingMovementRequest = {
    val ducrForm = cacheMap.getEntry[Ducr](Ducr.id).getOrElse(Ducr(""))
    val goodsDate =
      cacheMap.getEntry[GoodsDateForm](MovementFormsAndIds.goodsDateId)
    val location =
      cacheMap
        .getEntry[LocationForm](MovementFormsAndIds.locationId)
        .getOrElse(LocationForm(None, None, None, None))
    val transport =
      cacheMap
        .getEntry[TransportForm](MovementFormsAndIds.transportId)
        .getOrElse(TransportForm(None, None, None))

    // TODO: ucrType is hardcoded need to UPDATE after we allow user input for mucr
    InventoryLinkingMovementRequest(
      messageCode =
        if (choice.value.equals(AllowedChoiceValues.Arrival) || choice.value
              .equals(AllowedChoiceValues.Departure))
          choice.value
        else "",
      agentDetails =
        Some(AgentDetails(eori = Some(eori), agentLocation = location.agentLocation, agentRole = location.agentRole)),
      ucrBlock = UcrBlock(ucr = ducrForm.ducr, ucrType = "D"),
      goodsLocation = location.goodsLocation.getOrElse(""),
      goodsArrivalDateTime =
        if (choice.value.equals("EAL") && goodsDate.isDefined)
          Some(extractDateTime(goodsDate.getOrElse(GoodsDateForm("", "", "", None, None))))
        else None,
      goodsDepartureDateTime =
        if (choice.value.equals("EDL") && goodsDate.isDefined)
          Some(extractDateTime(goodsDate.getOrElse(GoodsDateForm("", "", "", None, None))))
        else None,
      shedOPID = location.shed,
      masterUCR = None,
      masterOpt = None,
      movementReference = None,
      transportDetails = Some(
        TransportDetails(
          transportID = transport.transportId,
          transportMode = transport.transportMode,
          transportNationality = transport.transportNationality
        )
      )
    )
  }

  private def extractDateTime(form: GoodsDateForm): String =
    LocalDateTime
      .of(
        form.year.toInt,
        form.month.toInt,
        form.day.toInt,
        form.hour.getOrElse("00").toInt,
        form.minute.getOrElse("00").toInt
      )
      .toString + ":00"

}
