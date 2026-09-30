package com.prannsss.loginactivity;

import java.util.Arrays;

public class MobiusRenderer {

    private final int width = 45;
    private final int height = 21;

    public String render(double rotationA, double rotationB) {

        char[] output = new char[width * height];
        float[] zBuffer = new float[width * height];

        Arrays.fill(output, ' ');
        Arrays.fill(zBuffer, 0);

        double sinA = Math.sin(rotationA);
        double cosA = Math.cos(rotationA);
        double sinB = Math.sin(rotationB);
        double cosB = Math.cos(rotationB);

        String luminance = ".,-~:;=!*#$@";

        for (double u = 0; u < Math.PI * 2; u += 0.035) {

            for (double v = -0.65; v <= 0.65; v += 0.045) {

                double cosU = Math.cos(u);
                double sinU = Math.sin(u);

                double cosHalfU = Math.cos(u / 2);
                double sinHalfU = Math.sin(u / 2);

                double radius =
                        1.8 + v * cosHalfU;

                double x =
                        radius * cosU;

                double y =
                        radius * sinU;

                double z =
                        v * sinHalfU;

                double rotatedY =
                        y * cosA - z * sinA;

                double rotatedZ =
                        y * sinA + z * cosA;

                double finalX =
                        x * cosB + rotatedZ * sinB;

                double finalZ =
                        -x * sinB + rotatedZ * cosB;

                double perspective =
                        1.0 / (4.5 + finalZ);

                int screenX =
                        (int) (
                                width / 2
                                        + finalX * 25 * perspective
                        );

                int screenY =
                        (int) (
                                height / 2
                                        + rotatedY * 16 * perspective
                        );

                if (screenX < 0
                        || screenX >= width
                        || screenY < 0
                        || screenY >= height) {
                    continue;
                }

                double light =
                        0.5 + 0.5 * (
                                cosU * cosHalfU
                                        + sinU * sinHalfU
                        );

                int luminanceIndex =
                        (int) (
                                light * (luminance.length() - 1)
                        );

                if (luminanceIndex < 0) {
                    luminanceIndex = 0;
                }

                if (luminanceIndex >= luminance.length()) {
                    luminanceIndex = luminance.length() - 1;
                }

                int index =
                        screenX + width * screenY;

                float depth =
                        (float) (
                                1.0 / (4.5 + finalZ)
                        );

                if (depth > zBuffer[index]) {

                    zBuffer[index] = depth;

                    output[index] =
                            luminance.charAt(luminanceIndex);
                }
            }
        }

        return convertToFrame(output);
    }

    private String convertToFrame(char[] output) {

        StringBuilder frame = new StringBuilder();

        for (int y = 0; y < height; y++) {
            frame.append(output, y * width, width);
            frame.append('\n');
        }

        return frame.toString();
    }
}