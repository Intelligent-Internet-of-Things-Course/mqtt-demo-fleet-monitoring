/*
 * Java GPX Library (@__identifier__@).
 * Copyright (c) @__year__@ Franz Wilhelmstötter
 *
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
 *
 * Author:
 *    Franz Wilhelmstötter (franz.wilhelmstoetter@gmail.com)
 */
package io.jenetics.jpx;

/**
 * Some string helper methods.
 *
 * @author <a href="mailto:franz.wilhelmstoetter@gmail.com">Franz Wilhelmstötter</a>
 * @version 2.0
 * @since 1.5.2
 */
final class Strings {
	private Strings() {
	}

	/**
	 * Trims the given input string. If the {@code value} is empty or {@code null},
	 * {@code null} is returned.
	 *
	 * @param value the string value to trim
	 * @return the trimmed string value
	 */
	static String trim(final String value) {
		return value != null && !value.isBlank() ? value.strip() : null;
	}

}
