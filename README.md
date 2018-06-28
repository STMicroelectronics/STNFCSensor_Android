# ST NFC Sensor

This repository contains the ST NFC Sensor app source code.

The ST NFC Sensor application, available for [iOS](https://itunes.apple.com/us/app/st-nfc-sensor/id1377274569?mt=8) and [Android](https://play.google.com/store/apps/details?id=com.st.smartTag), shows the data exported by sensor nodes via the NFC protocol.

It allows you to configure and read data from any system running the [FP-SNS-SMARTAG1](http://www.st.com/en/product/fp-sns-smartag1) function pack (for example, the [STEVAL-SMARTAG1](http://www.st.com/en/product/steval-smartag1) evaluation board).

You can configure the app by choosing the sampling intervals, the sensor data logged and the conditions to trigger data logging.

After configuration, the app shows data in informative plots, identifies significant events, such as high acceleration and changes in orientation, and exports data to a
csv file.

The embedded [ST25DV](http://www.st.com/st25dv) series Dynamic NFC Tag energy harvesting feature allows
using the [STEVAL-SMARTAG1](http://www.st.com/en/product/steval-smartag1) without a battery and reading sensor data in one shot
mode.


## Download the source

```Shell
git clone https://github.com/STMicroelectronics-CentralLabs/STNFCSensor_Android
```


## License

Copyright (c) 2018  STMicroelectronics – All rights reserved
The STMicroelectronics corporate logo is a trademark of STMicroelectronics

Redistribution and use in source and binary forms, with or without modification,
are permitted provided that the following conditions are met:

- Redistributions of source code must retain the above copyright notice, this list of conditions
and the following disclaimer.

- Redistributions in binary form must reproduce the above copyright notice, this list of
conditions and the following disclaimer in the documentation and/or other materials provided
with the distribution.

- Neither the name nor trademarks of STMicroelectronics International N.V. nor any other
STMicroelectronics company nor the names of its contributors may be used to endorse or
promote products derived from this software without specific prior written permission.

- All of the icons, pictures, logos and other images that are provided with the source code
in a directory whose title begins with st_images may only be used for internal purposes and
shall not be redistributed to any third party or modified in any way.

- Any redistributions in binary form shall not include the capability to display any of the
icons, pictures, logos and other images that are provided with the source code in a directory
whose title begins with st_images.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR
IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY
AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER
OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY
OF SUCH DAMAGE.
