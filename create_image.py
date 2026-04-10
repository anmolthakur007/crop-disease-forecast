# -*- coding: utf-8 -*-
# Create a simple 1x1 white pixel PNG image
import base64

# Minimal valid 1x1 pixel PNG (white background)
png_base64 = "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8z8DwHwAFBQIAX8jx0gAAAABJRU5ErkJggg=="
png_data = base64.b64decode(png_base64)

with open("test_image.png", "wb") as f:
    f.write(png_data)

print(f"Test image created: test_image.png, Size: {len(png_data)} bytes")
