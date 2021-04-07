package org.komapper.core.dsl

import org.komapper.core.dsl.element.Operand
import org.komapper.core.dsl.element.SortItem
import org.komapper.core.dsl.expression.AggregateFunction
import org.komapper.core.dsl.expression.AliasExpression
import org.komapper.core.dsl.expression.ArithmeticExpression
import org.komapper.core.dsl.expression.PropertyExpression
import org.komapper.core.dsl.expression.StringFunction

fun <T : Any> PropertyExpression<T>.asc(): PropertyExpression<T> {
    if (this is SortItem.Property.Asc) {
        return this
    }
    return SortItem.Property.Asc(this)
}

fun <T : Any> PropertyExpression<T>.desc(): PropertyExpression<T> {
    if (this is SortItem.Property.Desc) {
        return this
    }
    return SortItem.Property.Desc(this)
}

fun asc(alias: CharSequence): CharSequence {
    return SortItem.Alias.Asc(alias.toString())
}

fun desc(alias: CharSequence): CharSequence {
    return SortItem.Alias.Desc(alias.toString())
}

infix fun <T : Any> PropertyExpression<T>.alias(alias: String): PropertyExpression<T> {
    return AliasExpression(this, alias)
}

fun <T : Any> avg(c: PropertyExpression<T>): PropertyExpression<Double> {
    return AggregateFunction.Avg(c)
}

fun count(): PropertyExpression<Long> {
    return AggregateFunction.CountAsterisk
}

fun <T : Any> count(property: PropertyExpression<T>): PropertyExpression<Long> {
    return AggregateFunction.Count(property)
}

fun <T : Any> max(property: PropertyExpression<T>): PropertyExpression<T> {
    return AggregateFunction.Max(property)
}

fun <T : Any> min(property: PropertyExpression<T>): PropertyExpression<T> {
    return AggregateFunction.Min(property)
}

fun <T : Any> sum(property: PropertyExpression<T>): PropertyExpression<T> {
    return AggregateFunction.Sum(property)
}

infix operator fun <T : Number> PropertyExpression<T>.plus(value: T): PropertyExpression<T> {
    val left = Operand.Property(this)
    val right = Operand.Parameter(this, value)
    return ArithmeticExpression.Plus(this, left, right)
}

infix operator fun <T : Number> PropertyExpression<T>.plus(other: PropertyExpression<T>): PropertyExpression<T> {
    val left = Operand.Property(this)
    val right = Operand.Property(other)
    return ArithmeticExpression.Plus(this, left, right)
}

infix operator fun <T : Number> PropertyExpression<T>.minus(value: T): PropertyExpression<T> {
    val left = Operand.Property(this)
    val right = Operand.Parameter(this, value)
    return ArithmeticExpression.Minus(this, left, right)
}

infix operator fun <T : Number> PropertyExpression<T>.minus(other: PropertyExpression<T>): PropertyExpression<T> {
    val left = Operand.Property(this)
    val right = Operand.Property(other)
    return ArithmeticExpression.Minus(this, left, right)
}

infix operator fun <T : Number> PropertyExpression<T>.times(value: T): PropertyExpression<T> {
    val left = Operand.Property(this)
    val right = Operand.Parameter(this, value)
    return ArithmeticExpression.Times(this, left, right)
}

infix operator fun <T : Number> PropertyExpression<T>.times(other: PropertyExpression<T>): PropertyExpression<T> {
    val left = Operand.Property(this)
    val right = Operand.Property(other)
    return ArithmeticExpression.Times(this, left, right)
}

infix operator fun <T : Number> PropertyExpression<T>.div(value: T): PropertyExpression<T> {
    val left = Operand.Property(this)
    val right = Operand.Parameter(this, value)
    return ArithmeticExpression.Div(this, left, right)
}

infix operator fun <T : Number> PropertyExpression<T>.div(other: PropertyExpression<T>): PropertyExpression<T> {
    val left = Operand.Property(this)
    val right = Operand.Property(other)
    return ArithmeticExpression.Div(this, left, right)
}

infix operator fun <T : Number> PropertyExpression<T>.rem(value: T): PropertyExpression<T> {
    val left = Operand.Property(this)
    val right = Operand.Parameter(this, value)
    return ArithmeticExpression.Rem(this, left, right)
}

infix operator fun <T : Number> PropertyExpression<T>.rem(other: PropertyExpression<T>): PropertyExpression<T> {
    val left = Operand.Property(this)
    val right = Operand.Property(other)
    return ArithmeticExpression.Rem(this, left, right)
}

fun concat(left: PropertyExpression<String>, right: String): PropertyExpression<String> {
    val o1 = Operand.Property(left)
    val o2 = Operand.Parameter(left, right)
    return StringFunction.Concat(left, o1, o2)
}

fun concat(left: String, right: PropertyExpression<String>): PropertyExpression<String> {
    val o1 = Operand.Parameter(right, left)
    val o2 = Operand.Property(right)
    return StringFunction.Concat(right, o1, o2)
}

fun concat(left: PropertyExpression<String>, right: PropertyExpression<String>): PropertyExpression<String> {
    val o1 = Operand.Property(left)
    val o2 = Operand.Property(right)
    return StringFunction.Concat(right, o1, o2)
}
