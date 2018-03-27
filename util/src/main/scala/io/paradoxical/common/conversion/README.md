Conversions
---

The conversions package contains a partial port of Twitter's bijection library: https://github.com/twitter/bijection. 

### Bijections
A Bijection is a type that represents a reversible conversion between two other types. For instance, String and Array\[Byte\]. 

Bijections are incredibly useful for writing generic programs and interfaces where type conversion is key. For instance, Bijections would be useful when interfacing with MySQL, where only some Scala types are mapped to MySQL types. In this case, Bijections serve as a hidden layer between the much richer Scala type system and something simpler.

(Contrived) Example of generic DAO/DTO conversions:
```scala
import io.paradoxical.common.conversion.bijection._
import io.paradoxical.common.conversion.bijection.Bijection._

trait DAO { def toDTO: DTO }
trait DTO { def toDAO: DAO }

case class ActualDAO() extends DAO {
  override def toDTO = ActualDTO()
}
case class ActualDTO() extends DTO {
  override def toDAO = ActualDAO()
}

implicit val daoToDto: Bijection[DAO, DTO] = new AbstractBijection[DAO, DTO] {
  override def apply(a: DAO): DTO = a.toDTO
  override def invert(b: DTO): DAO = b.toDAO
}

def save(dto: ActualDTO)(implicit bij: ImplicitBijection[DAO, DTO]) {
  actuallySave(bij.invert(dto))
}

val dto = ActualDTO()
save(dto)
```

### Injections
Injections, like Bijections, represent a conversion of types. However, with Injections, the inverse conversion doesn't necessarily exist. A simple example of an Injection is converting an Integer to a Long. All Integers are Longs but not all Longs are Integers. We can represent this relationship with an Injection\[Int, Long\]

Another (contrived) example
```scala
import io.paradoxical.common.conversion.injection._
import io.paradoxical.common.conversion.injection.Injection._
import java.util.UUID

// We now implicit have an Injection[UUID, String]

case class PersonDAO(id: String)
case class PersonDTO(id: UUID)


```

### Composing
Bijections and Injections can be composed with several types to create new Bijections and Injections. 

```scala
import io.paradoxical.common.conversion.bijection.Bijection._
import io.paradoxical.common.conversion.injection.Injection._
import scala.util.Random

val longToExcitedString = stringToLong andThen (str => s"$str!!!") // Injection[Long, String]

val r = new Random()
val randomBigInt = longToBigInt compose (() => r.nextLong())
```

### Syntactic Sugar & Conversions

Example
```scala
import io.paradoxical.common.conversion.bijection.Base64String
import io.paradoxical.common.conversion.bijection.Bijection._
import io.paradoxical.common.conversion.injection.Injection._
import io.paradoxical.common.conversion.Conversion._
import scala.util.Random

// Shorthand for andThen and compose

val longToExcitedString = stringToLong | (str => s"$str!!!") // Injection[Long, String]

val r = new Random()
val randomBigInt = longToBigInt & (() => r.nextLong())

// Using .as[T] when a Bijection/Injection is available
// Array[Byte] => Base64String is available so...

val myByteArray: Array[Byte] = ???
val myBase64String = myByteArray.as[Base64String].str
```

### Use Cases
* Defining common conversions in a single place
* Writing generalized API clients
* Simplifying type conversion with an idiomatic API (cleaner code)
