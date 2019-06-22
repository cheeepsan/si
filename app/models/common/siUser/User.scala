package models.common.siUser


case class User(id: Option[Long], username: String, avatar: Option[String]) {

}

object User {
  def apply(id: Option[Long], username: String, avatar: Option[String]): User = new User(id, username, avatar)

  def unapply(arg: User): Option[(Option[Long], String, Option[String])] = None
}


case class LoginObject(username: String, address: String) {

}

object LoginObject {
  def apply(username: String, address: String): LoginObject = new LoginObject(username, address)

  def unapply(arg: LoginObject): Option[(String, String)] = None
}

