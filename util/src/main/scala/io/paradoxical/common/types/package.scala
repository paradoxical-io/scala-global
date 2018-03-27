package io.paradoxical.common

package object types {
  type Id[+A] = A

  type Not[T] = {
    type Evidence[U] = U NotTypeOf T
  }
  type NotNothing[T] = Not[Nothing]#Evidence[T]
  type NotUnit[T] = Not[Unit]#Evidence[T]
}