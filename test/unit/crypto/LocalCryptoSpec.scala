/*
 * Copyright 2020 HM Revenue & Customs
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

package crypto

import config.{AppConfig, MongoEncryption}
import org.mockito.Mockito.when
import uk.gov.hmrc.crypto.PlainText
import unit.base.UnitSpec

class LocalCryptoSpec extends UnitSpec {

  private val config = mock[AppConfig]

  "encrypt()" should {

    "enable encrypt local" in {
      when(config.mongoEncryption).thenReturn(MongoEncryption(true, Some("YjQ+NiViNGY4V2l2cSxnCg==")))
      (new LocalCrypto(config)).encrypt(PlainText("hello")).toString mustBe "Crypted(gUfxIXsmMDAbdTgm36BmEg==)"
    }

    "error on missing config" in {
      when(config.mongoEncryption).thenReturn(MongoEncryption(true, None))

      val caught = intercept[RuntimeException] {
        new LocalCrypto(config).encrypt(PlainText("hello"))
      }
      caught.getMessage mustBe "Missing config: 'mongodb.encryption.enabled'"
    }
  }

}
