package autoproxy.test

import org.specs2.mutable.Specification

class CaseWithMixinSpecs extends Specification {

  "Proxy alternative to case class copy" should {
    "simple example" in {
      val entity = CaseWithMixin("777")
      val proxy = new CaseWithMixinProxy(entity)

      proxy.id mustEqual (entity.id)
      proxy.version must equalTo(entity.version)
      proxy.timeStamp must equalTo(entity.timeStamp + 1)

      success
    }
  }
}