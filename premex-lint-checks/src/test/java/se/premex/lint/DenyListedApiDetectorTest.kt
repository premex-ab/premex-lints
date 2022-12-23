package se.premex.lint

import com.android.tools.lint.checks.infrastructure.LintDetectorTest
import com.android.tools.lint.checks.infrastructure.TestLintTask
import com.android.tools.lint.checks.infrastructure.TestMode.Companion.FULLY_QUALIFIED
import com.android.tools.lint.checks.infrastructure.TestMode.Companion.IMPORT_ALIAS
import com.android.tools.lint.detector.api.Detector
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.io.File

// Adapted from https://gist.github.com/JakeWharton/19b1e7b8d5c648b2935ba89148b791ed
@RunWith(JUnit4::class)
class DenyListedApiDetectorTest : LintDetectorTest() {

    // Suggested checks inspired by https://github.com/slackhq/slack-lints
    val xmlconfig = xml(
        "blocklist.xml",
        """
            <?xml version="1.0" encoding="utf-8"?>
            <BlockList>
                <Blocked>
                    <ClassName>io.reactivex.rxjava3.core.Observable</ClassName>
                    <FunctionName>hide</FunctionName>
                    <ErrorMessage><![CDATA[There should be no reason to defend against downcasting an Observable to  an implementation type like Relay or Subject in a closed codebase. Doing this incurs  needless runtime memory and performance overhead. Relays and Subjects both extend from  Observable and can be supplied to functions accepting Observable directly. When  returning a Relay or Subject, declare the return type explicitly as Observable  (e.g., fun foo(): Observable<Foo> = fooRelay).]]></ErrorMessage>
                </Blocked>
                <Blocked>
                    <ClassName>io.reactivex.rxjava3.core.Flowable</ClassName>
                    <FunctionName>hide</FunctionName>
                    <ErrorMessage><![CDATA[There should be no reason to defend against downcasting an Flowable to an implementation type like FlowableProcessor in a closed codebase. Doing this incurs needless runtime memory and performance overhead. FlowableProcessor extends from Flowable and can be supplied to functions accepting Flowable directly. When returning a FlowableProcessor, declare the return type explicitly as Flowable (e.g., fun foo(): Flowable<Foo> = fooProcessor).]]></ErrorMessage>
                </Blocked>
                <Blocked>
                    <ClassName>io.reactivex.rxjava3.core.Completable</ClassName>
                    <FunctionName>hide</FunctionName>
                    <ErrorMessage><![CDATA[There should be no reason to defend against downcasting a Completable to an implementation type like CompletableSubject in a closed codebase. Doing this incurs needless runtime memory and performance overhead. CompletableSubject extends from Completable and can be supplied to functions accepting Completable directly. When returning a CompletableSubject, declare the return type explicitly as Completable (e.g., fun foo(): Completable<Foo> = fooSubject).]]></ErrorMessage>
                </Blocked>
                <Blocked>
                    <ClassName>io.reactivex.rxjava3.core.Maybe</ClassName>
                    <FunctionName>hide</FunctionName>
                    <ErrorMessage><![CDATA[There should be no reason to defend against downcasting a Maybe to an implementation type like MaybeSubject in a closed codebase. Doing this incurs needless runtime memory and performance overhead. MaybeSubject extends from Maybe and can be supplied to functions accepting Maybe directly. When returning a MaybeSubject, declare the return type explicitly as Maybe (e.g., fun foo(): Maybe<Foo> = fooSubject).]]></ErrorMessage>
                </Blocked>
                <Blocked>
                    <ClassName>io.reactivex.rxjava3.core.Single</ClassName>
                    <FunctionName>hide</FunctionName>
                    <ErrorMessage><![CDATA[There should be no reason to defend against downcasting a Single to an implementation type like SingleSubject in a closed codebase. Doing this incurs needless runtime memory and performance overhead. SingleSubject extends from Single and can be supplied to functions accepting Single directly. When returning a SingleSubject, declare the return type explicitly as Single (e.g., fun foo(): Single<Foo> = fooSubject).]]></ErrorMessage>
                </Blocked>
                <Blocked>
                    <ClassName>androidx.core.content.ContextCompat</ClassName>
                    <FunctionName>getDrawable</FunctionName>
                    <Parameter>android.content.Context</Parameter>
                    <Parameter>int</Parameter>
                    <ErrorMessage>Use Context#getDrawableCompat() instead</ErrorMessage>
                </Blocked>
                <Blocked>
                    <ClassName>androidx.core.content.res.ResourcesCompat</ClassName>
                    <FunctionName>getDrawable</FunctionName>
                    <Parameter>android.content.Context</Parameter>
                    <Parameter>int</Parameter>
                    <ErrorMessage>Use Context#getDrawableCompat() instead</ErrorMessage>
                </Blocked>
                <Blocked>
                    <ClassName>android.support.test.espresso.matcher.ViewMatchers</ClassName>
                    <FunctionName>withId</FunctionName>
                    <Parameter>int</Parameter>
                    <ErrorMessage>Consider matching the content description instead. IDs are implementation details of how a screen is built, not how it works. You can't tell a user to click on the button with ID 428194727 so our tests should not be doing that.</ErrorMessage>
                </Blocked>
                <Blocked>
                    <ClassName>android.view.View</ClassName>
                    <FunctionName>setOnClickListener</FunctionName>
                    <Parameter>android.view.View.OnClickListener</Parameter>
                    <Arguments>null</Arguments>
                    <ErrorMessage>This fails to also set View#isClickable. Use View#clearOnClickListener() instead</ErrorMessage>
                </Blocked>
                <Blocked>
                    <ClassName>kotlinx.coroutines.flow.FlowKt__CollectKt</ClassName>
                    <FunctionName>launchIn</FunctionName>
                    <ErrorMessage>Use the structured concurrent CoroutineScope#launch and Flow#collect APIs instead of reactive Flow#onEach and Flow#launchIn. Suspend calls like Flow#collect can be refactored into standalone suspend funs and mixed in with regular control flow in a suspend context, but calls that invoke CoroutineScope#launch and Flow#collect at the same time hide the suspend context, encouraging the developer to continue working in the reactive domain.</ErrorMessage>
                </Blocked>
                <Blocked>
                    <ClassName>androidx.viewpager2.widget.ViewPager2</ClassName>
                    <FunctionName>setId</FunctionName>
                    <Parameter>int</Parameter>
                    <Arguments>ViewCompat.generateViewId()</Arguments>
                    <ErrorMessage>Use an id defined in resources or a statically created instead of generating with ViewCompat.generateViewId(). See https://issuetracker.google.com/issues/185820237</ErrorMessage>
                </Blocked>
                <Blocked>
                    <ClassName>androidx.viewpager2.widget.ViewPager2</ClassName>
                    <FunctionName>setId</FunctionName>
                    <Parameter>int</Parameter>
                    <Arguments>View.generateViewId()</Arguments>
                    <ErrorMessage>Use an id defined in resources or a statically created instead of generating with View.generateViewId(). See https://issuetracker.google.com/issues/185820237</ErrorMessage>
                </Blocked>
                <Blocked>
                    <ClassName>java.util.LinkedList</ClassName>
                    <FunctionName><![CDATA[<init>]]></FunctionName>
                    <ErrorMessage>For a stack/queue/double-ended queue use ArrayDeque, for a list use ArrayList. Both are more efficient internally.</ErrorMessage>
                </Blocked>
                <Blocked>
                    <ClassName>java.util.Stack</ClassName>
                    <FunctionName><![CDATA[<init>]]></FunctionName>
                    <ErrorMessage>For a stack use ArrayDeque which is more efficient internally.</ErrorMessage>
                </Blocked>
                <Blocked>
                    <ClassName>java.util.Vector</ClassName>
                    <FunctionName><![CDATA[<init>]]></FunctionName>
                    <ErrorMessage>For a vector use ArrayList or ArrayDeque which are more efficient internally.</ErrorMessage>
                </Blocked>
                <Blocked>
                    <ClassName>io.reactivex.rxjava3.schedulers.Schedulers</ClassName>
                    <FunctionName>newThread</FunctionName>
                    <ErrorMessage>Use a scheduler which wraps a cached set of threads. There should be no reason to be arbitrarily creating threads on Android.</ErrorMessage>
                </Blocked>
                <Blocked>
                    <ClassName>kotlinx.coroutines.rx3.RxCompletableKt</ClassName>
                    <FunctionName>rxCompletable</FunctionName>
                    <Parameter>kotlin.coroutines.CoroutineContext</Parameter>
                    <Parameter><![CDATA[kotlin.jvm.functions.Function2<? super kotlinx.coroutines.CoroutineScope,? super kotlin.coroutines.Continuation<? super kotlin.Unit>,? extends java.lang.Object>]]></Parameter>
                    <Arguments>*</Arguments>
                    <ErrorMessage>rxCompletable defaults to Dispatchers.Default, which will silently introduce multithreading. Provide an explicit dispatcher. Dispatchers.Unconfined is usually the best choice, as it behaves in an rx-y way.</ErrorMessage>
                </Blocked>
                <Blocked>
                    <ClassName>kotlinx.coroutines.rx3.RxMaybeKt</ClassName>
                    <FunctionName>rxMaybe</FunctionName>
                        <Parameter>kotlin.coroutines.CoroutineContext</Parameter>
                        <Parameter><![CDATA[kotlin.jvm.functions.Function2<? super kotlinx.coroutines.CoroutineScope,? super kotlin.coroutines.Continuation<? super T>,? extends java.lang.Object>]]></Parameter>
                    <ErrorMessage>rxMaybe defaults to Dispatchers.Default, which will silently introduce multithreading. Provide an explicit dispatcher. Dispatchers.Unconfined is usually the best choice, as it behaves in an rx-y way.</ErrorMessage>
                </Blocked>
                <Blocked>
                    <ClassName>kotlinx.coroutines.rx3.RxSingleKt</ClassName>
                    <FunctionName>rxSingle</FunctionName>
                        <Parameter>kotlin.coroutines.CoroutineContext</Parameter>
                        <Parameter><![CDATA[kotlin.jvm.functions.Function2<? super kotlinx.coroutines.CoroutineScope,? super kotlin.coroutines.Continuation<? super T>,? extends java.lang.Object>]]></Parameter>
                    <Arguments>*</Arguments>
                    <ErrorMessage>rxSingle defaults to Dispatchers.Default, which will silently introduce multithreading. Provide an explicit dispatcher. Dispatchers.Unconfined is usually the best choice, as it behaves in an rx-y way.</ErrorMessage>
                </Blocked>
                <Blocked>
                    <ClassName>kotlinx.coroutines.rx3.RxObservableKt</ClassName>
                    <FunctionName>rxObservable</FunctionName>
                        <Parameter>kotlin.coroutines.CoroutineContext</Parameter>
                        <Parameter><![CDATA[kotlin.jvm.functions.Function2<? super kotlinx.coroutines.channels.ProducerScope<T>,? super kotlin.coroutines.Continuation<? super kotlin.Unit>,? extends java.lang.Object>]]></Parameter>
                    <Arguments>*</Arguments>
                    <ErrorMessage>rxObservable defaults to Dispatchers.Default, which will silently introduce multithreading. Provide an explicit dispatcher. Dispatchers.Unconfined is usually the best choice, as it behaves in an rx-y way.</ErrorMessage>
                </Blocked>
            </BlockList>
        """.trimIndent()
    )

    override fun lint(): TestLintTask {
        return super.lint()
            .configureOption(
                DenyListedApiDetector.BLOCK_FILE_LIST,
                File("blocklist.xml")
            )
    }

    override fun getIssues() = listOf(DenyListedApiDetector.ISSUE)

    override fun getDetector(): Detector = DenyListedApiDetector()

    @Test
    fun `flag function with params in deny list`() {
        lint()
            .files(
                CONTEXT_STUB,
                DRAWABLE_STUB,
                CONTEXT_COMPAT_STUB,
                xmlconfig,
                kotlin(
                    """
          package foo

          import android.content.Context
          import android.graphics.drawable.Drawable
          import androidx.core.content.ContextCompat

          class SomeView(context: Context) {
            init {
              ContextCompat.getDrawable(context, 42)
            }
          }
          """
                )
                    .indented()
            )
            .run()
            .expect(
                """
        src/foo/SomeView.kt:9: Error: Use Context#getDrawableCompat() instead [BlockListedApi]
            ContextCompat.getDrawable(context, 42)
            ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        1 errors, 0 warnings
        """
                    .trimIndent()
            )
    }

    @Test
    fun `allow function not in deny list`() {
        lint()
            .files(
                OBSERVABLE_STUB,
                TEST_OBSERVER_STUB,
                RX_RULE_STUB,
                xmlconfig,
                kotlin(
                    """
          package cash

          import io.reactivex.rxjava3.core.Observable
          import io.reactivex.rxjava3.observers.TestObserver
          import com.squareup.util.rx3.test.test
          import com.squareup.util.rx3.test.RxRule

          class FooTest {
            @get:Rule val rxRule = RxRule()

            fun test() {
              observable().test(rxRule).assertValue(42)
            }

            fun observable(): Observable<String> = TODO()
          }
          """
                )
                    .indented()
            )
            .run()
            .expectClean()
    }

    @Test
    fun `setOnClickListener with null argument in deny list`() {
        lint()
            .files(
                VIEW_STUB,
                xmlconfig,
                kotlin(
                    """
          package foo

          import android.view.View;

          class SomeView(view: View) {
            init {
              view.setOnClickListener(null);
            }
          }
          """
                )
                    .indented()
            )
            .run()
            .expect(
                """
        src/foo/SomeView.kt:7: Error: This fails to also set View#isClickable. Use View#clearOnClickListener() instead [BlockListedApi]
            view.setOnClickListener(null);
            ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        1 errors, 0 warnings
        """
                    .trimIndent()
            )
    }

    @Test
    fun `setOnClickListener with non-null argument not in deny list`() {
        lint()
            .files(
                VIEW_STUB,
                xmlconfig,
                kotlin(
                    """
          package foo

          import android.view.View;

          class SomeView(view: View) {
            init {
              view.setOnClickListener {
                // do something
              }
            }
          }
          """
                )
                    .indented()
            )
            .run()
            .expectClean()
    }

    @Test
    fun `setId with explicit id not in deny list`() {
        lint()
            .files(
                VIEWPAGER2_STUB,
                xmlconfig,
                kotlin(
                    """
          package foo

          import androidx.viewpager2.widget.ViewPager2;

          class SomeView(view: ViewPager2) {
            init {
              view.setId(1)
            }
          }
          """
                )
                    .indented()
            )
            .run()
            .expectClean()
    }

    @Test
    fun `setId with ViewCompat#generateViewId() in deny list`() {
        lint()
            .files(
                VIEWCOMPAT_STUB,
                VIEWPAGER2_STUB,
                xmlconfig,
                kotlin(
                    """
          package foo

          import androidx.viewpager2.widget.ViewPager2;
          import androidx.core.view.ViewCompat;

          class SomeView(view: ViewPager2) {
            init {
              view.setId(ViewCompat.generateViewId())
            }
          }
          """
                )
                    .indented()
            )
            .skipTestModes(FULLY_QUALIFIED, IMPORT_ALIAS) // TODO relies on non-qualified matching.
            .run()
            .expect(
                """
        src/foo/SomeView.kt:8: Error: Use an id defined in resources or a statically created instead of generating with ViewCompat.generateViewId(). See https://issuetracker.google.com/issues/185820237 [BlockListedApi]
            view.setId(ViewCompat.generateViewId())
            ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        1 errors, 0 warnings
        """
                    .trimIndent()
            )
    }

    @Test
    fun `setId with View#generateViewId() in deny list`() {
        lint()
            .files(
                VIEW_STUB,
                VIEWPAGER2_STUB,
                xmlconfig,
                kotlin(
                    """
          package foo

          import androidx.viewpager2.widget.ViewPager2;
          import android.view.View;

          class SomeView(view: ViewPager2) {
            init {
              view.setId(View.generateViewId())
            }
          }
          """
                )
                    .indented()
            )
            .skipTestModes(FULLY_QUALIFIED, IMPORT_ALIAS) // TODO relies on non-qualified matching.
            .run()
            .expect(
                """
        src/foo/SomeView.kt:8: Error: Use an id defined in resources or a statically created instead of generating with View.generateViewId(). See https://issuetracker.google.com/issues/185820237 [BlockListedApi]
            view.setId(View.generateViewId())
            ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        1 errors, 0 warnings
        """
                    .trimIndent()
            )
    }

    @Test
    fun errorLinkedList() {
        lint()
            .files(
                xmlconfig,
                kotlin(
                    """
          package foo

          import java.util.LinkedList

          class SomeClass {
            val stuff = LinkedList<String>()
          }
          """
                )
                    .indented()
            )
            .run()
            .expect(
                """
        src/foo/SomeClass.kt:6: Error: For a stack/queue/double-ended queue use ArrayDeque, for a list use ArrayList. Both are more efficient internally. [BlockListedApi]
          val stuff = LinkedList<String>()
                      ~~~~~~~~~~~~~~~~~~~~
        1 errors, 0 warnings
        """
                    .trimIndent()
            )
    }

    @Test
    fun errorStack() {
        lint()
            .files(
                xmlconfig,
                kotlin(
                    """
          package foo

          import java.util.Stack

          class SomeClass {
            val stuff = Stack<String>()
          }
          """
                )
                    .indented()
            )
            .run()
            .expect(
                """
        src/foo/SomeClass.kt:6: Error: For a stack use ArrayDeque which is more efficient internally. [BlockListedApi]
          val stuff = Stack<String>()
                      ~~~~~~~~~~~~~~~
        1 errors, 0 warnings
        """
                    .trimIndent()
            )
    }

    @Test
    fun errorVector() {
        lint()
            .files(
                xmlconfig,
                kotlin(
                    """
          package foo

          import java.util.Vector

          class SomeClass {
            val stuff = Vector<String>()
          }
          """
                )
                    .indented()
            )
            .run()
            .expect(
                """
        src/foo/SomeClass.kt:6: Error: For a vector use ArrayList or ArrayDeque which are more efficient internally. [BlockListedApi]
          val stuff = Vector<String>()
                      ~~~~~~~~~~~~~~~~
        1 errors, 0 warnings
        """
                    .trimIndent()
            )
    }

    @Test
    fun schedulersNewThread() {
        lint()
            .files(
                SCHEDULERS_STUB,
                xmlconfig,
                kotlin(
                    """
          package foo

          import io.reactivex.rxjava3.schedulers.Schedulers

          class SomeClass {
            val scheduler = Schedulers.newThread()
          }
          """
                )
                    .indented()
            )
            .run()
            .expect(
                """
        src/foo/SomeClass.kt:6: Error: Use a scheduler which wraps a cached set of threads. There should be no reason to be arbitrarily creating threads on Android. [BlockListedApi]
          val scheduler = Schedulers.newThread()
                          ~~~~~~~~~~~~~~~~~~~~~~
        1 errors, 0 warnings
        """
                    .trimIndent()
            )
    }

    @Test
    fun rxCompletableParameterless() {
        lint()
            .files(
                EMPTY_COROUTINE_CONTEXT_STUB,
                COROUTINE_CONTEXT_STUB,
                COROUTINE_SCOPE_STUB,
                COMPLETABLE_STUB,
                RX_COMPLETABLE_STUB,
                xmlconfig,
                kotlin(
                    """
          package foo

          import kotlinx.coroutines.rx3.rxCompletable

          class SomeClass {
            val now = rxCompletable {}
          }
          """
                )
                    .indented()
            )
            .run()
            .expect(
                """
        src/foo/SomeClass.kt:6: Error: rxCompletable defaults to Dispatchers.Default, which will silently introduce multithreading. Provide an explicit dispatcher. Dispatchers.Unconfined is usually the best choice, as it behaves in an rx-y way. [BlockListedApi]
          val now = rxCompletable {}
                    ~~~~~~~~~~~~~~~~
        1 errors, 0 warnings
        """
                    .trimIndent()
            )
    }

    @Test
    fun rxSingleParameterless() {
        lint()
            .files(
                EMPTY_COROUTINE_CONTEXT_STUB,
                COROUTINE_CONTEXT_STUB,
                COROUTINE_SCOPE_STUB,
                SINGLE_STUB,
                RX_SINGLE_STUB,
                xmlconfig,
                kotlin(
                    """
          package foo

          import kotlinx.coroutines.rx3.rxSingle

          class SomeClass {
            val now = rxSingle { "a" }
          }
          """
                )
                    .indented()
            )
            .run()
            .expect(
                """
        src/foo/SomeClass.kt:6: Error: rxSingle defaults to Dispatchers.Default, which will silently introduce multithreading. Provide an explicit dispatcher. Dispatchers.Unconfined is usually the best choice, as it behaves in an rx-y way. [BlockListedApi]
          val now = rxSingle { "a" }
                    ~~~~~~~~~~~~~~~~
        1 errors, 0 warnings
        """
                    .trimIndent()
            )
    }

    @Test
    fun rxMaybeParameterless() {
        lint()
            .files(
                EMPTY_COROUTINE_CONTEXT_STUB,
                COROUTINE_CONTEXT_STUB,
                COROUTINE_SCOPE_STUB,
                MAYBE_STUB,
                RX_MAYBE_STUB,
                xmlconfig,
                kotlin(
                    """
          package foo

          import kotlinx.coroutines.rx3.rxMaybe

          class SomeClass {
            val now = rxMaybe { "a" }
          }
          """
                )
                    .indented()
            )
            .run()
            .expect(
                """
        src/foo/SomeClass.kt:6: Error: rxMaybe defaults to Dispatchers.Default, which will silently introduce multithreading. Provide an explicit dispatcher. Dispatchers.Unconfined is usually the best choice, as it behaves in an rx-y way. [BlockListedApi]
          val now = rxMaybe { "a" }
                    ~~~~~~~~~~~~~~~
        1 errors, 0 warnings
        """
                    .trimIndent()
            )
    }

    @Test
    fun rxObservableParameterless() {
        lint()
            .files(
                EMPTY_COROUTINE_CONTEXT_STUB,
                COROUTINE_CONTEXT_STUB,
                COROUTINE_SCOPE_STUB,
                TEST_OBSERVER_STUB,
                OBSERVABLE_STUB,
                PRODUCER_STUB,
                RX_OBSERVABLE_STUB,
                xmlconfig,
                kotlin(
                    """
          package foo

          import kotlinx.coroutines.rx3.rxObservable

          class SomeClass {
            val now = rxObservable { send("a") }
          }
          """
                )
                    .indented()
            )
            .run()
            .expect(
                """
        src/foo/SomeClass.kt:6: Error: rxObservable defaults to Dispatchers.Default, which will silently introduce multithreading. Provide an explicit dispatcher. Dispatchers.Unconfined is usually the best choice, as it behaves in an rx-y way. [BlockListedApi]
          val now = rxObservable { send("a") }
                    ~~~~~~~~~~~~~~~~~~~~~~~~~~
        1 errors, 0 warnings
        """
                    .trimIndent()
            )
    }

    @Test
    fun rxCompletableWithParameters() {
        lint()
            .files(
                EMPTY_COROUTINE_CONTEXT_STUB,
                COROUTINE_CONTEXT_STUB,
                COROUTINE_SCOPE_STUB,
                COMPLETABLE_STUB,
                RX_COMPLETABLE_STUB,
                xmlconfig,
                kotlin(
                    """
          package foo

          import kotlinx.coroutines.rx3.rxCompletable

          object MyDispatcher : CoroutineContext

          class SomeClass {
            val now = rxCompletable(MyDispatcher) {}
          }
          """
                )
                    .indented()
            )
            .run()
            .expectClean()
    }

    companion object {
        private val OBSERVABLE_STUB =
            java(
                """
        package io.reactivex.rxjava3.core;

        import io.reactivex.rxjava3.observers.TestObserver;

        public abstract class Observable<T> {
          public static <T> Observable<T> just(T item) {}
          public final TestObserver<T> test() {}
          public final TestObserver<T> test(boolean dispose) {}
        }
      """
            )
                .indented()

        private val TEST_OBSERVER_STUB =
            java(
                """
        package io.reactivex.rxjava3.observers;

        public class TestObserver<T> {
          public final assertValue(T value) {}
        }
      """
            )
                .indented()

        private val RX_RULE_STUB =
            kotlin(
                """
        package com.squareup.util.rx3.test

        import io.reactivex.rxjava3.core.Observable

        class RxRule {
          fun <T : Any> newObserver(): RecordingObserver<T> = TODO()
        }

        fun <T: Any> Observable<T>.test(rxRule: RxRule): RecordingObserver<T>
      """
            )
                .indented()

        private val CONTEXT_COMPAT_STUB =
            java(
                """
        package androidx.core.content;

        import android.graphics.drawable.Drawable;
        import android.content.Context;

        public class ContextCompat {
          public static Drawable getDrawable(Context context, int id) {}
        }
      """
            )
                .indented()

        private val DRAWABLE_STUB =
            java(
                """
        package android.graphics.drawable;

        public class Drawable {}
      """
            )
                .indented()

        private val CONTEXT_STUB =
            java(
                """
        package android.content;

        public class Context {}
      """
            )
                .indented()

        private val VIEW_STUB =
            java(
                """
        package android.view;

        public class View {

          public static int generateViewId() { return 0; }

          public void setOnClickListener(View.OnClickListener l) {}

          public interface OnClickListener {}
        }
      """
            )
                .indented()

        private val VIEWCOMPAT_STUB =
            java(
                """
        package androidx.core.view;

        public class ViewCompat {
          public static int generateViewId() { return 0; }
        }
      """
            )
                .indented()

        private val VIEWPAGER2_STUB =
            java(
                """
        package androidx.viewpager2.widget;

        public class ViewPager2 {
          public void setId(int id) {}
        }
      """
            )
                .indented()

        private val SCHEDULERS_STUB =
            java(
                """
        package io.reactivex.rxjava3.schedulers;

        public final class Schedulers {
          public static Object newThread() {
            return null;
          }
        }
      """
            )
                .indented()

        private val COROUTINE_CONTEXT_STUB =
            kotlin(
                """
        package kotlin.coroutines

        interface CoroutineContext
      """
            )
                .indented()
        private val EMPTY_COROUTINE_CONTEXT_STUB =
            kotlin(
                """
        package kotlin.coroutines

        object EmptyCoroutineContext : CoroutineContext
      """
            )
                .indented()
        private val COROUTINE_SCOPE_STUB =
            kotlin(
                """
        package kotlinx.coroutines

        interface CoroutineScope
      """
            )
                .indented()
        private val COMPLETABLE_STUB =
            java(
                """
        package io.reactivex.rxjava3.core;

        public final class Completable {
          Completable() {}
        }
      """
            )
                .indented()

        private val RX_COMPLETABLE_STUB =
            kotlin(
                """
        package kotlinx.coroutines.rx3

        import kotlin.coroutines.CoroutineContext
        import kotlin.coroutines.EmptyCoroutineContext
        import kotlinx.coroutines.CoroutineScope

        class RxCompletable {}

        public fun rxCompletable(
            context: CoroutineContext = EmptyCoroutineContext,
            block: suspend CoroutineScope.() -> Unit
        ): Completable {
          return Completable
        }
      """
            )
                .indented()

        private val SINGLE_STUB =
            java(
                """
        package io.reactivex.rxjava3.core;

        public final class Single<T> {
          Single() {}
        }
      """
            )
                .indented()

        private val RX_SINGLE_STUB =
            kotlin(
                """
        package kotlinx.coroutines.rx3

        import kotlin.coroutines.CoroutineContext
        import kotlin.coroutines.EmptyCoroutineContext
        import kotlinx.coroutines.CoroutineScope

        class RxSingle {}

        public fun <T> rxSingle(
            context: CoroutineContext = EmptyCoroutineContext,
            block: suspend CoroutineScope.() -> T
        ): Single<T> {
          return Single<T>()
        }
      """
            )
                .indented()

        private val MAYBE_STUB =
            java(
                """
        package io.reactivex.rxjava3.core;

        public final class Maybe<T> {
          Maybe() {}
        }
      """
            )
                .indented()

        private val RX_MAYBE_STUB =
            kotlin(
                """
        package kotlinx.coroutines.rx3

        import kotlin.coroutines.CoroutineContext
        import kotlin.coroutines.EmptyCoroutineContext
        import kotlinx.coroutines.CoroutineScope

        class rxMaybe {}

        public fun <T> rxMaybe(
            context: CoroutineContext = EmptyCoroutineContext,
            block: suspend CoroutineScope.() -> T?
        ): Maybe<T> {
          return Maybe<T>()
        }
      """
            )
                .indented()

        private val PRODUCER_STUB =
            kotlin(
                """
        package kotlinx.coroutines.channels

        object ProducerScope<T> {
          suspend fun send(value: T)
        }
      """
            )
                .indented()

        private val RX_OBSERVABLE_STUB =
            kotlin(
                """
        package kotlinx.coroutines.rx3

        import kotlin.coroutines.CoroutineContext
        import kotlin.coroutines.EmptyCoroutineContext
        import kotlinx.coroutines.CoroutineScope
        import kotlinx.coroutines.channels.ProducerScope

        class RxObservable {}

        public fun <T> rxObservable(
            context: CoroutineContext = EmptyCoroutineContext,
            block: suspend ProducerScope<T>.() -> Unit
        ): Observable<T> {
          return Observable<T>()
        }
      """
            )
                .indented()

    }
}
