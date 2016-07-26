/*******************************************************************************
 * Copyright (c) 2016 Bruno Medeiros and other Contributors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Bruno Medeiros - initial API and implementation
 *******************************************************************************/
package melnorme.utilbox.core.fntypes;

import static melnorme.utilbox.core.Assert.AssertNamespace.assertFail;
import static melnorme.utilbox.core.Assert.AssertNamespace.assertNotNull;
import static melnorme.utilbox.core.Assert.AssertNamespace.assertTrue;
import static melnorme.utilbox.core.CoreUtil.areEqual;

import melnorme.utilbox.misc.HashcodeUtil;

public class Result<DATA, EXC extends Throwable> {
	
	protected final DATA resultValue;
	/** Note: resultException is either a EXC, or a RuntimeException. */
	protected final Throwable resultException;
	
	public static <DATA, EXC extends Throwable> Result<DATA, EXC> fromValue(DATA resultValue) {
		return new Result<>(resultValue);
	}
	
	public static <DATA, EXC extends Throwable> Result<DATA, EXC> fromException(EXC exception) {
		return new Result<>(null, exception);
	}
	
	public static <DATA, EXC extends Throwable> Result<DATA, EXC> fromRuntimeException(RuntimeException exception) {
		return new Result<>(null, exception, null);
	}
	
	public Result(DATA resultValue) {
		this(resultValue, null);
	}
	
	public Result(DATA resultValue, EXC resultException) {
		this(resultValue, resultException, null);
	}
	
	protected Result(DATA resultValue, Throwable resultException, @SuppressWarnings("unused") Void dummy) {
		this.resultValue = resultValue;
		this.resultException = resultException;
		assertTrue(resultValue == null || resultException == null);
	}
	
	@Override
	public boolean equals(Object obj) {
		if(this == obj) return true;
		if(!(obj instanceof Result)) return false;
		
		Result<?, ?> other = (Result<?, ?>) obj;
		
		return 
			areEqual(resultValue, other.resultValue) && 
			areEqual(resultException, other.resultException);
	}
	
	@Override
	public int hashCode() {
		return HashcodeUtil.combinedHashCode(resultValue, resultException);
	}
	
	/* -----------------  ----------------- */
	
	public DATA get() throws EXC {
		throwIfExceptionResult();
		return resultValue;
	}
	
	public boolean isSuccessful() {
		return resultException == null;
	}
	
	public boolean isException() {
		return !isSuccessful();
	}
	
	public DATA getSuccessful() {
		assertTrue(isSuccessful());
		try {
			return get();
		} catch(Throwable e) {
			throw assertFail();
		}
	}
	
	public DATA getOrNull() {
		try {
			return get();
		} catch(RuntimeException e) {
			throw e;
		} catch(Throwable e) {
			return null;
		}
	}
	
	public Throwable getResultException() {
		return resultException;
	}
	
	@SuppressWarnings("unchecked")
	protected void throwIfExceptionResult() throws EXC  {
		if(resultException instanceof RuntimeException) {
			throw (RuntimeException) resultException;
		}
		if(resultException != null) {
			throw (EXC) resultException;
		}
	}
	
	public static <RET, EXC  extends Throwable> Result<RET, EXC> callToResult(Callable2<RET, EXC> callable2) {
		assertNotNull(callable2);
		try {
			return Result.fromValue(callable2.invoke());
		} catch(Throwable e) {
			@SuppressWarnings("unchecked")
			EXC exc = (EXC) e;
			return new Result<>(null, exc);
		}
	}
	
}