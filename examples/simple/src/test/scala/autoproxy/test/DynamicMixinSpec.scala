package autoproxy.test

import org.specs2.mutable.Specification

class DynamicMixinSpec extends Specification {
  "The dynamic mixin" should {
    "work" in {
      val theTrait = new DynamicMixin {
        val theString = "bippy"
        val theInt = 42
      }
      val user = new DynamicMixinUser(theTrait)

      user.theString mustEqual "bippy"
      user.theInt mustEqual 42
    }
  }
}