package com.example.bingoetage.colors

import android.graphics.Color

class ColorConverter(val colorRGB: Int)
{
    val hsv = convertToHSV(colorRGB)

    companion object
    {
        fun convertToHSV(colorRGB: Int): FloatArray
        {
            val hsv = FloatArray(3)
            Color.RGBToHSV(Color.red(colorRGB), Color.green(colorRGB), Color.blue(colorRGB), hsv)
            return hsv
        }
        fun convertToRGB(hsv: FloatArray): Int
        {
            return Color.HSVToColor(hsv)

        }
        fun interpolate(input:Float, colorA: ColorConverter, colorB: ColorConverter): ColorConverter
        {
            val hsv = FloatArray(3)
            for (i in 0..2)
            {
                hsv[i] = colorA.hsv[i] + input * ( colorB.hsv[i] - colorA.hsv[i])
            }
            return ColorConverter(convertToRGB(hsv))
        }
        fun interpolateFromRGB(input:Float, colorA: Int, colorB: Int): Int
        {
            val hsv = FloatArray(3)
            val hsvA = convertToHSV(colorA)
            val hsvB = convertToHSV(colorB)

            for (i in 0..2)
            {
                hsv[i] = hsvA[i] + input * ( hsvB[i] - hsvA[i])
            }
            return convertToRGB(hsv)
        }
    }
}