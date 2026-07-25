package com.capystick.app.widget

import com.capystick.domain.repository.WidgetRepository
import com.capystick.domain.widget.GetWidgetContent
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface WidgetEntryPoint {
    fun getWidgetContentUseCase(): GetWidgetContent

    fun getWidgetRepository(): WidgetRepository
}
