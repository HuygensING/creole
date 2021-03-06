package nl.knaw.huc.di.rd.tag.creole

/*-
 * #%L
 * creole
 * =======
 * Copyright (C) 2016 - 2019 HuC DI (KNAW)
 * =======
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
class ValidationResult {

    private var unexpectedEvent: Event? = null
    private var success: Boolean = false

    fun setSuccess(success: Boolean): ValidationResult {
        this.success = success
        return this
    }

    fun setUnexpectedEvent(unexpectedEvent: Event?): ValidationResult {
        this.unexpectedEvent = unexpectedEvent
        return this
    }

    fun isSuccess(): Boolean {
        return success
    }

    fun getUnexpectedEvent(): Event? {
        return unexpectedEvent
    }
}
