/*
 * Project: Conductor
 * Copyright (C) 2022 alf.labs gmail com,
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.alfray.conductor.v2.dagger

import javax.inject.Scope

/**
 * Indicates that a component or class is tied to a script's sub-component lifecycle.
 *
 * A scope is used to tag a component. Every class tagged with the same scope becomes
 * a "singleton" tied to the lifecycle of that component. When a given sub-component is
 * tagged with a scope, all the modules provided classes should logically have the same
 * scope (if they are singletons within this scope) or no scope (if they are not singletons).
 *
 * There is no need for a "@ConductorScope", since that's what the basic @Singleton does.
 */
@Scope
@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
annotation class Script2kScope
