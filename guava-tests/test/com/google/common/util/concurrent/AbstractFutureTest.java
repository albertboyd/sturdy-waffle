/*
 * Copyright (C) 2011 The Guava Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.common.util.concurrent;

import static org.junit.contrib.truth.Truth.ASSERT;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

import java.util.concurrent.ExecutionException;

/**
 * Tests for {@link AbstractFuture}.
 *
 * @author Brian Stoler
 */
public class AbstractFutureTest extends TestCase {
  public void testSuccess() throws ExecutionException, InterruptedException {
    final Object value = new Object();
    assertSame(value, new AbstractFuture<Object>() {
      {
        set(value);
      }
    }.get());
  }

  public void testException() throws InterruptedException {
    final Throwable failure = new Throwable();
    AbstractFuture<String> future = new AbstractFuture<String>() {
      {
        setException(failure);
      }
    };

    ExecutionException ee1 = getExpectingExecutionException(future);
    ExecutionException ee2 = getExpectingExecutionException(future);

    // Ensure we get a unique execution exception on each get
    assertNotSame(ee1, ee2);

    assertSame(failure, ee1.getCause());
    assertSame(failure, ee2.getCause());

    checkStackTrace(ee1);
    checkStackTrace(ee2);
  }

  public void testCancel_notDoneNoInterrupt() {
    InterruptibleFuture future = new InterruptibleFuture();
    assertTrue(future.cancel(false));
    assertTrue(future.isCancelled());
    assertTrue(future.isDone());
    assertFalse(future.wasInterrupted);
  }

  public void testCancel_notDoneInterrupt() {
    InterruptibleFuture future = new InterruptibleFuture();
    assertTrue(future.cancel(true));
    assertTrue(future.isCancelled());
    assertTrue(future.isDone());
    assertTrue(future.wasInterrupted);
  }

  public void testCancel_done() {
    AbstractFuture<String> future = new AbstractFuture<String>() {
      {
        set("foo");
      }
    };
    assertFalse(future.cancel(true));
    assertFalse(future.isCancelled());
    assertTrue(future.isDone());
  }

  private void checkStackTrace(ExecutionException e) {
    // Our call site for get() should be in the trace.
    int index = findStackFrame(
        e, getClass().getName(), "getExpectingExecutionException");

    ASSERT.that(index).isNotEqualTo(0);

    // Above our method should be the call to get(). Don't assert on the class
    // because it could be some superclass.
    ASSERT.that(e.getStackTrace()[index - 1].getMethodName()).isEqualTo("get");
  }

  private static int findStackFrame(
      ExecutionException e, String clazz, String method) {
    StackTraceElement[] elements = e.getStackTrace();
    for (int i = 0; i < elements.length; i++) {
      StackTraceElement element = elements[i];
      if (element.getClassName().equals(clazz)
          && element.getMethodName().equals(method)) {
        return i;
      }
    }
    AssertionFailedError failure =
        new AssertionFailedError("Expected element " + clazz + "." + method
            + " not found in stack trace");
    failure.initCause(e);
    throw failure;
  }

  private ExecutionException getExpectingExecutionException(
      AbstractFuture<String> future) throws InterruptedException {
    try {
      String got = future.get();
      fail("Expected exception but got " + got);
    } catch (ExecutionException e) {
      return e;
    }

    // unreachable, but compiler doesn't know that fail() always throws
    return null;
  }

  private static final class InterruptibleFuture
      extends AbstractFuture<String> {
    boolean wasInterrupted;

    @Override protected void interruptTask() {
      wasInterrupted = true;
    }
  }
}
