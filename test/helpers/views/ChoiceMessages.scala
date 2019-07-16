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

package helpers.views

trait ChoiceMessages {

  val title: String = "movement.choice.title"

  val arrivalDecLabel: String = "movement.choice.EAL.label"
  val departureDecLabel: String = "movement.choice.EDL.label"
  val associateDecLabel: String = "movement.choice.ASS.label"
  val disassociateDecLabel: String = "movement.choice.EAC.label"
  val shutMucrLabel: String = "movement.choice.shutMucr.label"
  val choiceEmpty: String = "choicePage.input.error.empty"
  val choiceError: String = "choicePage.input.error.incorrectValue"
}
