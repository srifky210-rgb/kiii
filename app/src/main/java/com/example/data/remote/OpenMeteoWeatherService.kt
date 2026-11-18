package com.example.data.remote

import android.content.Context
import android.location.Geocoder
import android.os.Build
import com.squareup.moshi.Json
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume

data class OpenMeteoForecastResponse(
    @Json(name = "latitude") val latitude: Double? = null,
    @Json(name = "longitude") val longitude: Double? = null,
    @Json(name = "timezone") val timezone: String? = null,
    @Json(name = "current") val current: CurrentWeatherDto? = null,
    @Json(name = "hourly") val hourly: HourlyWeatherDto? = null
)

data class CurrentWeatherDto(
    @Json(name = "time") val time: String? = null,
    @Json(name = "temperature_2m") val temperature2m: Double? = null,
    @Json(name = "apparent_temperature") val apparentTemperature: Double? = null,
    @Json(name = "relative_humidity_2m") val relativeHumidity2m: Int? = null,
    @Json(name = "precipitation") val precipitation: Double? = null,
    @Json(name = "weather_code") val weatherCode: Int? = null,
    @Json(name = "wind_speed_10m") val windSpeed10m: Double? = null
)

data class HourlyWeatherDto(
    @Json(name = "time") val time: List<String> = emptyList(),
    @Json(name = "temperature_2m") val temperature2m: List<Double> = emptyList(),
    @Json(name = "precipitation_probability") val precipitationProbability: List<Int> = emptyList(),
    @Json(name = "precipitation") val precipitation: List<Double> = emptyList(),
    @Json(name = "weather_code") val weatherCode: List<Int> = emptyList()
)

interface OpenMeteoApi {
    @GET("v1/forecast")
    suspend fun getForecast(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("current") current: String = "temperature_2m,apparent_temperature,relative_humidity_2m,precipitation,weather_code,wind_speed_10m",
        @Query("hourly") hourly: String = "temperature_2m,precipitation_probability,precipitation,weather_code",
        @Query("forecast_days") forecastDays: Int = 1,
        @Query("timezone") timezone: String = "auto"
    ): OpenMeteoForecastResponse
}

enum class RunWeatherSeverity {
    IDEAL,
    CAUTION,
    WARNING
}

data class RunWeatherAdvice(
    val headline: String,
    val detail: String,
    val severity: RunWeatherSeverity
)

data class HourlyWeatherSlot(
    val hourLabel: String,
    val emoji: String,
    val temperatureC: Double,
    val precipProbabilityPct: Int
)

data class LocalWeatherState(
    val isLoading: Boolean = false,
    val hasLoadedData: Boolean = false,
    val errorMessage: String? = null,
    val locationLabel: String = "Lokasi Anda",
    val isUsingFallbackLocation: Boolean = false,
    val temperatureC: Double = 0.0,
    val apparentTemperatureC: Double = 0.0,
    val humidityPct: Int = 0,
    val precipitationMm: Double = 0.0,
    val precipProbabilityPct: Int = 0,
    val windSpeedKmh: Double = 0.0,
    val weatherCode: Int = 0,
    val conditionLabel: String = "Memuat cuaca...",
    val conditionEmoji: String = "🌤️",
    val runAdvice: RunWeatherAdvice = RunWeatherAdvice(
        headline = "Memeriksa kondisi cuaca",
        detail = "Menyiapkan saran waktu lari berdasarkan suhu & curah hujan.",
        severity = RunWeatherSeverity.IDEAL
    ),
    val hourlySlots: List<HourlyWeatherSlot> = emptyList(),
    val lastUpdatedMillis: Long = 0L
)

object WmoWeatherMapper {
    fun describeCode(code: Int): Pair<String, String> = when (code) {
        0 -> "Cerah" to "☀️"
        1 -> "Cerah Berawan" to "🌤️"
        2 -> "Berawan Sebagian" to "⛅"
        3 -> "Mendung" to "☁️"
        45, 48 -> "Berkabut" to "🌫️"
        51, 53, 55, 56, 57 -> "Gerimis" to "🌦️"
        61, 63, 80, 81 -> "Hujan Ringan" to "🌧️"
        65, 66, 67, 82 -> "Hujan Lebat" to "⛈️"
        71, 73, 75, 77, 85, 86 -> "Salju" to "❄️"
        95, 96, 99 -> "Badai Petir" to "🌩️"
        else -> "Berawan" to "🌤️"
    }

    fun evaluateRunningAdvice(
        temperatureC: Double,
        precipitationMm: Double,
        precipProbabilityPct: Int,
        weatherCode: Int
    ): RunWeatherAdvice {
        return when {
            weatherCode in listOf(95, 96, 99) -> RunWeatherAdvice(
                headline = "Tunda Lari Outdoor • Badai Petir",
                detail = "Potensi petir terdeteksi. Lakukan pemanasan Penjas di dalam ruangan.",
                severity = RunWeatherSeverity.WARNING
            )
            precipitationMm >= 0.8 || precipProbabilityPct >= 65 -> RunWeatherAdvice(
                headline = "Potensi Hujan Tinggi ($precipProbabilityPct%)",
                detail = String.format(
                    Locale.US,
                    "Curah hujan %.1f mm. Pertimbangkan rute berteduh atau jadwal lari di jam lebih cerah.",
                    precipitationMm
                ),
                severity = RunWeatherSeverity.WARNING
            )
            precipProbabilityPct >= 35 || precipitationMm >= 0.1 -> RunWeatherAdvice(
                headline = "Waspada Gerimis ($precipProbabilityPct%)",
                detail = "Gunakan sepatu anti-selip dan jaket tipis saat berlari di luar.",
                severity = RunWeatherSeverity.CAUTION
            )
            temperatureC >= 32.0 -> RunWeatherAdvice(
                headline = "Panas Terik (${temperatureC.toInt()}°C)",
                detail = "Bawa air minum, kurangi pace target, dan pilih rute teduh.",
                severity = RunWeatherSeverity.CAUTION
            )
            temperatureC >= 28.0 -> RunWeatherAdvice(
                headline = "Cukup Hangat (${temperatureC.toInt()}°C)",
                detail = "Kondisi baik untuk lari santai. Pastikan hidrasi sebelum & sesudah lari.",
                severity = RunWeatherSeverity.IDEAL
            )
            temperatureC in 16.0..27.9 -> RunWeatherAdvice(
                headline = "Ideal untuk Lari Outdoor!",
                detail = "Suhu nyaman dan risiko hujan rendah. Waktu terbaik mengejar target pace!",
                severity = RunWeatherSeverity.IDEAL
            )
            else -> RunWeatherAdvice(
                headline = "Udara Sejuk (${temperatureC.toInt()}°C)",
                detail = "Lakukan pemanasan dinamis ekstra 5 menit sebelum mulai berlari.",
                severity = RunWeatherSeverity.IDEAL
            )
        }
    }

    fun mapResponseToState(
        response: OpenMeteoForecastResponse,
        locationLabel: String,
        isUsingFallbackLocation: Boolean,
        nowMillis: Long = System.currentTimeMillis()
    ): LocalWeatherState {
        val current = response.current
        val hourly = response.hourly

        val temp = current?.temperature2m ?: hourly?.temperature2m?.firstOrNull() ?: 27.0
        val apparent = current?.apparentTemperature ?: temp
        val humidity = current?.relativeHumidity2m ?: 70
        val precipMm = current?.precipitation ?: hourly?.precipitation?.firstOrNull() ?: 0.0
        val code = current?.weatherCode ?: hourly?.weatherCode?.firstOrNull() ?: 1
        val wind = current?.windSpeed10m ?: 0.0

        val currentHourPrefix = current?.time?.take(13) ?: run {
            val cal = Calendar.getInstance().apply { timeInMillis = nowMillis }
            String.format(
                Locale.US,
                "%04d-%02d-%02dT%02d",
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH) + 1,
                cal.get(Calendar.DAY_OF_MONTH),
                cal.get(Calendar.HOUR_OF_DAY)
            )
        }

        val startIdx = if (hourly != null && hourly.time.isNotEmpty()) {
            val match = hourly.time.indexOfFirst { it >= currentHourPrefix }
            if (match >= 0) match else 0
        } else 0

        val currentPrecipProb = hourly?.precipitationProbability?.getOrNull(startIdx)
            ?: if (precipMm >= 0.5) 75 else if (precipMm > 0.0) 40 else 10

        val slots = if (hourly != null && hourly.time.isNotEmpty()) {
            (startIdx until minOf(startIdx + 5, hourly.time.size)).map { idx ->
                val rawTime = hourly.time[idx]
                val hourLabel = rawTime.substringAfter('T', rawTime).take(5)
                val slotCode = hourly.weatherCode.getOrElse(idx) { code }
                val (_, slotEmoji) = describeCode(slotCode)
                HourlyWeatherSlot(
                    hourLabel = hourLabel,
                    emoji = slotEmoji,
                    temperatureC = hourly.temperature2m.getOrElse(idx) { temp },
                    precipProbabilityPct = hourly.precipitationProbability.getOrElse(idx) { currentPrecipProb }
                )
            }
        } else {
            emptyList()
        }

        val (condLabel, condEmoji) = describeCode(code)
        val advice = evaluateRunningAdvice(
            temperatureC = temp,
            precipitationMm = precipMm,
            precipProbabilityPct = currentPrecipProb,
            weatherCode = code
        )

        return LocalWeatherState(
            isLoading = false,
            hasLoadedData = true,
            errorMessage = null,
            locationLabel = locationLabel,
            isUsingFallbackLocation = isUsingFallbackLocation,
            temperatureC = temp,
            apparentTemperatureC = apparent,
            humidityPct = humidity,
            precipitationMm = precipMm,
            precipProbabilityPct = currentPrecipProb,
            windSpeedKmh = wind,
            weatherCode = code,
            conditionLabel = condLabel,
            conditionEmoji = condEmoji,
            runAdvice = advice,
            hourlySlots = slots,
            lastUpdatedMillis = nowMillis
        )
    }
}

class OpenMeteoWeatherRepository(context: Context) {
    private val appContext = context.applicationContext

    private val api: OpenMeteoApi by lazy {
        val client = OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .build()
        val moshi = Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()
        Retrofit.Builder()
            .baseUrl("https://api.open-meteo.com/")
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(OpenMeteoApi::class.java)
    }

    suspend fun fetchLocalWeather(
        coordinates: Pair<Double, Double>?
    ): Result<LocalWeatherState> = withContext(Dispatchers.IO) {
        val usingFallback = coordinates == null
        val lat = coordinates?.first ?: DEFAULT_LAT
        val lon = coordinates?.second ?: DEFAULT_LON

        runCatching {
            val response = api.getForecast(latitude = lat, longitude = lon)
            val label = if (usingFallback) {
                "Jakarta (Default GPS)"
            } else {
                resolveLocalityLabel(lat, lon)
            }
            WmoWeatherMapper.mapResponseToState(
                response = response,
                locationLabel = label,
                isUsingFallbackLocation = usingFallback
            )
        }
    }

    private suspend fun resolveLocalityLabel(lat: Double, lon: Double): String {
        if (!Geocoder.isPresent()) return "Lokasi GPS"
        val geocoder = Geocoder(appContext, Locale.forLanguageTag("id-ID"))
        return try {
            val addresses = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                suspendCancellableCoroutine { cont ->
                    geocoder.getFromLocation(lat, lon, 1) { list ->
                        if (cont.isActive) cont.resume(list)
                    }
                }
            } else {
                @Suppress("DEPRECATION")
                geocoder.getFromLocation(lat, lon, 1).orEmpty()
            }
            val addr = addresses.firstOrNull()
            listOfNotNull(
                addr?.subLocality?.takeIf { it.isNotBlank() },
                addr?.locality?.takeIf { it.isNotBlank() } ?: addr?.subAdminArea?.takeIf { it.isNotBlank() }
            ).distinct().take(2).joinToString(", ").ifBlank { "Lokasi GPS" }
        } catch (_: Exception) {
            "Lokasi GPS"
        }
    }

    companion object {
        const val DEFAULT_LAT = -6.2088
        const val DEFAULT_LON = 106.8456
    }
}
