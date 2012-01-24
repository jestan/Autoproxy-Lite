package autoproxy.test

import autoproxy.annotation.proxy

trait Base {
  val version = 0
}

trait CreatedUpdated {
  val timeStamp = 0
}

case class CaseWithMixin(id: String) extends Base with CreatedUpdated {
  def this() = this("")
}

class CaseWithMixinProxy(@proxy instance: CaseWithMixin) extends CaseWithMixin {
  override val timeStamp = instance.timeStamp + 1
}