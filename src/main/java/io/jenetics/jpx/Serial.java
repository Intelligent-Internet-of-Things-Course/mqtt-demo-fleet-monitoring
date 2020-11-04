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

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.StreamCorruptedException;

/**
 * The shared serialization delegate for this package.
 *
 * @author <a href="mailto:franz.wilhelmstoetter@gmail.com">Franz Wilhelmstötter</a>
 * @version 2.0
 * @since 1.2
 */
final class Serial implements Externalizable {
	private static final long serialVersionUID = 1L;

	static final byte BOUNDS = 1;
	static final byte COPYRIGHT = 2;
	static final byte DEGREES = 3;
	static final byte DGPS_STATION = 4;
	static final byte EMAIL = 5;
	static final byte GPX_TYPE = 6;
	static final byte LATITUDE = 7;
	static final byte LENGTH = 8;
	static final byte LINK = 9;
	static final byte LONGITUDE = 10;
	static final byte METADATA = 11;
	static final byte PERSON = 12;
	static final byte ROUTE = 13;
	static final byte SPEED = 14;
	static final byte TRACK = 15;
	static final byte TRACK_SEGMENT = 16;
	static final byte UINT = 17;
	static final byte WAY_POINT = 18;

	/**
	 * The type being serialized.
	 */
	private byte _type;

	/**
	 * The object being serialized.
	 */
	private Object _object;

	/**
	 * Constructor for deserialization.
	 */
	public Serial() {
	}

	/**
	 * Creates an instance for serialization.
	 *
	 * @param type  the type
	 * @param object  the object
	 */
	Serial(final byte type, final Object object) {
		_type = type;
		_object = object;
	}

	@Override
	public void writeExternal(final ObjectOutput out) throws IOException {
		out.writeByte(_type);
		switch (_type) {
			case BOUNDS: ((Bounds)_object).write(out); break;
			case COPYRIGHT: ((Copyright)_object).write(out); break;
			case DEGREES: ((Degrees)_object).write(out); break;
			case DGPS_STATION: ((DGPSStation)_object).write(out); break;
			case EMAIL: ((Email)_object).write(out); break;
			case GPX_TYPE: ((GPX)_object).write(out); break;
			case LATITUDE: ((Latitude)_object).write(out); break;
			case LENGTH: ((Length)_object).write(out); break;
			case LINK: ((Link)_object).write(out); break;
			case LONGITUDE: ((Longitude)_object).write(out); break;
			case METADATA: ((Metadata)_object).write(out); break;
			case PERSON: ((Person)_object).write(out); break;
			case ROUTE: ((Route)_object).write(out); break;
			case SPEED: ((Speed)_object).write(out); break;
			case TRACK: ((Track)_object).write(out); break;
			case TRACK_SEGMENT: ((TrackSegment)_object).write(out); break;
			case UINT: ((UInt)_object).write(out); break;
			case WAY_POINT: ((WayPoint)_object).write(out); break;
			default:
				throw new StreamCorruptedException(
					"Unknown serialized type: " + _type
				);
		}
	}

	@Override
	public void readExternal(final ObjectInput in) throws IOException {
		_type = in.readByte();
		switch (_type) {
			case BOUNDS: _object = Bounds.read(in); break;
			case COPYRIGHT: _object = Copyright.read(in); break;
			case DEGREES: _object = Degrees.read(in); break;
			case DGPS_STATION: _object = DGPSStation.read(in); break;
			case EMAIL: _object = Email.read(in); break;
			case GPX_TYPE: _object = GPX.read(in); break;
			case LATITUDE: _object = Latitude.read(in); break;
			case LENGTH: _object = Length.read(in); break;
			case LINK: _object = Link.read(in); break;
			case LONGITUDE: _object = Longitude.read(in); break;
			case METADATA: _object = Metadata.read(in); break;
			case PERSON: _object = Person.read(in); break;
			case ROUTE: _object = Route.read(in); break;
			case SPEED: _object = Speed.read(in); break;
			case TRACK: _object = Track.read(in); break;
			case TRACK_SEGMENT: _object = TrackSegment.read(in); break;
			case UINT: _object = UInt.read(in); break;
			case WAY_POINT: _object = WayPoint.read(in); break;
			default:
				throw new StreamCorruptedException(
					"Unknown serialized type: " + _type
				);
		}
	}

	private Object readResolve() {
		return _object;
	}

}
