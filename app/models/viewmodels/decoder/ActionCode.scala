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

package models.viewmodels.decoder

sealed abstract class ActionCode(
  override val code: String,
  override val status: String,
  override val contentKey: String
) extends CodeWithContentKey

object ActionCode {

  val codes: Set[ActionCode] = Set(AcknowledgedAndProcessed, PartiallyAcknowledgedAndProcessed, Rejected)

  case object AcknowledgedAndProcessed
      extends ActionCode(
        code = "1",
        status = "AcknowledgedAndProcessed",
        contentKey = "notifications.elem.content.inventoryLinkingControlResponse.AcknowledgedAndProcessed"
      )
  case object PartiallyAcknowledgedAndProcessed
      extends ActionCode(
        code = "2",
        status = "PartiallyAcknowledgedAndProcessed",
        contentKey = "notifications.elem.content.inventoryLinkingControlResponse.PartiallyAcknowledgedAndProcessed"
      )
  case object Rejected
      extends ActionCode(
        code = "3",
        status = "Rejected",
        contentKey = "notifications.elem.content.inventoryLinkingControlResponse.Rejected"
      )
}
