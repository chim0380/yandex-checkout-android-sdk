/*
 * The MIT License (MIT)
 * Copyright © 2018 NBCO Yandex.Money LLC
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the “Software”), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do so, subject to the
 * following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial
 * portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT
 * OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */

package ru.yandex.money.android.sdk.impl.payment.tokenize

import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.Timeout
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.inOrder
import org.mockito.Mockito.mock
import org.mockito.junit.MockitoJUnitRunner
import ru.yandex.money.android.sdk.Presenter
import ru.yandex.money.android.sdk.SdkException
import ru.yandex.money.android.sdk.UseCase
import ru.yandex.money.android.sdk.ViewModel
import ru.yandex.money.android.sdk.impl.contract.ContractErrorViewModel
import ru.yandex.money.android.sdk.impl.contract.ContractFailViewModel
import ru.yandex.money.android.sdk.impl.contract.ContractViewModel
import ru.yandex.money.android.sdk.on
import ru.yandex.money.android.sdk.payment.tokenize.TokenizeInputModel
import ru.yandex.money.android.sdk.payment.tokenize.TokenizeOutputModel
import ru.yandex.money.android.sdk.waitUntilEmpty
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit

@RunWith(MockitoJUnitRunner.StrictStubs::class)
class TokenizeControllerTest {

    @[Rule JvmField]
    val timeout = Timeout(1, TimeUnit.MINUTES)

    private val states = LinkedBlockingQueue<ViewModel>()

    private val input = TokenizeInputModel(1, false)

    @Mock
    private lateinit var useCase: UseCase<TokenizeInputModel, TokenizeOutputModel>
    @Mock
    private lateinit var successPresenter: Presenter<TokenizeOutputModel, ContractViewModel>
    @Mock
    private lateinit var errorPresenter: Presenter<Exception, ContractFailViewModel>

    private lateinit var controller: TokenizeController

    @Before
    fun setUp() {
        controller = TokenizeController(
                tokenizeUseCase = useCase,
                successPresenter = successPresenter,
                errorPresenter = errorPresenter,
                resultConsumer = { states.add(it) },
                logger = { _, _ ->  }
        )
    }

    @Test
    fun successPass() {
        // prepare
        val output = mock(TokenizeOutputModel::class.java)
        val viewModel = mock(ContractViewModel::class.java)
        on(useCase(input)).thenReturn(output)
        on(successPresenter(output)).thenReturn(viewModel)

        // invoke
        controller(input)

        // assert
        waitUntilEmpty(states)
        inOrder(useCase, successPresenter, errorPresenter).apply {
            verify(useCase).invoke(input)
            verify(successPresenter).invoke(output)
            verifyNoMoreInteractions()
        }
    }

    @Test
    fun failPass() {
        // prepare
        val exception = SdkException()
        on(useCase(input)).then { throw exception }
        on(errorPresenter.invoke(exception)).thenReturn(ContractErrorViewModel("err"))

        // invoke
        controller(input)

        // assert
        waitUntilEmpty(states)
        inOrder(useCase, successPresenter, errorPresenter).apply {
            verify(useCase).invoke(input)
            verify(errorPresenter).invoke(exception)
            verifyNoMoreInteractions()
        }
    }
}
