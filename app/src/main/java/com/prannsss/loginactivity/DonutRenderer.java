package com.prannsss.loginactivity;

import java.util.Arrays;

public class DonutRenderer {

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

        for (double theta = 0; theta < Math.PI * 2; theta += 0.07) {

            double sintheta = Math.sin(theta);
            double costheta = Math.cos(theta);

            for (double phi = 0; phi < Math.PI * 2; phi += 0.02) {

                double sinphi = Math.sin(phi);
                double cosphi = Math.cos(phi);

                double circleX = costheta + 2;
                double circleY = sintheta;

                double x =
                        circleX * (cosB * cosphi + sinA * sinB * sinphi)
                                - circleY * cosA * sinB;

                double y =
                        circleX * (sinB * cosphi - sinA * cosB * sinphi)
                                + circleY * cosA * cosB;

                double z =
                        5 + circleX * cosA * sinphi + circleY * sinA;

                double oneOverZ = 1 / z;

                int screenX =
                        (int) (width / 2 + 24 * oneOverZ * x);

                int screenY =
                        (int) (height / 2 + 12 * oneOverZ * y);

                double luminanceValue =
                        cosphi * costheta * sinB
                                - cosA * costheta * sinphi
                                - sinA * sintheta
                                + cosB * (
                                cosA * sintheta
                                        - sinA * costheta * sinphi
                        );

                if (luminanceValue > 0
                        && screenX >= 0
                        && screenX < width
                        && screenY >= 0
                        && screenY < height) {

                    int index = screenX + width * screenY;

                    if (oneOverZ > zBuffer[index]) {

                        zBuffer[index] = (float) oneOverZ;

                        int luminanceIndex =
                                (int) (luminanceValue * 8);

                        if (luminanceIndex < 0) {
                            luminanceIndex = 0;
                        }

                        if (luminanceIndex >= luminance.length()) {
                            luminanceIndex = luminance.length() - 1;
                        }

                        output[index] =
                                luminance.charAt(luminanceIndex);
                    }
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