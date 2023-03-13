/*
 * Project: Conductor
 * Copyright (C) 2023 alf.labs gmail com,
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

package com.alflabs.conductor.v1.script;

import com.alflabs.kv.IKeyValue;

import javax.inject.Inject;
import javax.inject.Provider;

public class VarFactory {
  private final Provider<IKeyValue> keyValueProvider;

  @Inject
  public VarFactory(Provider<IKeyValue> keyValueProvider) {
    this.keyValueProvider = checkNotNull(keyValueProvider, 1);
  }

  public Var create(int intValue, String varName) {
    return new Var(intValue, checkNotNull(varName, 2), checkNotNull(keyValueProvider.get(), 3));
  }

  public Var create(IIntValue intValueSupplier, String varName) {
    return new Var(checkNotNull(intValueSupplier, 1), checkNotNull(varName, 2), checkNotNull(keyValueProvider.get(), 3));
  }

  public Var create(String stringValue, String scriptName) {
    return new Var(checkNotNull(stringValue, 1), checkNotNull(scriptName, 2), checkNotNull(keyValueProvider.get(), 3));
  }

  private static <T> T checkNotNull(T reference, int argumentIndex) {
    if (reference == null) {
      throw new NullPointerException("@AutoFactory method argument is null but is not marked @Nullable. Argument index: " + argumentIndex);
    }
    return reference;
  }
}
