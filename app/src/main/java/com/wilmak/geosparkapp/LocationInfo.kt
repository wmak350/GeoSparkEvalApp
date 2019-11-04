package com.wilmak.geosparkapp

import java.io.Serializable

class LocationInfo(
   public var accuracy: Double? = -1.0,
   public var altitude: Double? = -1.0,
   public var bearing: Double? = -1.0,
   public var speed: Double? = -1.0,
   public var latitude: Double = -1.0,
   public var longitude: Double = -1.0,
   public var elpasedTime: Double = 0.0,
   public var time: Long = 0,
   public var provider: String = ""
) : Serializable {

}
