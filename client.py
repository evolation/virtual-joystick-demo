import io
import socket
import struct
from PIL import Image
import time
import argparse

from aiy.vision.inference import CameraInference
from aiy.vision.models import image_classification
from picamera import PiCamera



"""Camera image classification demo code.

Runs continuous image detection on the VisionBonnet and prints the object and
probability for top three objects.

Example:
image_classification_camera.py --num_frames 10
"""



def main():
    """Image classification camera inference example."""
    parser = argparse.ArgumentParser()
    parser.add_argument(
        '--num_frames',
        '-n',
        type=int,
        dest='num_frames',
        default=-1,
        help='Sets the number of frames to run for, otherwise runs forever.')

    parser.add_argument(
        '--num_objects',
        '-c',
        type=int,
        dest='num_objects',
        default=3,
        help='Sets the number of object interences to print.')

    args = parser.parse_args()

    def print_classes(classes, object_count):
        s = ''
        for index, (obj, prob) in enumerate(classes):
            if index > object_count - 1:
                break
            s += '%s=%1.2f\t|\t' % (obj, prob)
        print('%s\r' % s)

    s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    ip = '192.168.0.5'
    port = 1337
    print("Socket Ok")
    s.connect((ip, port))
    print("Connect Ok")
    connection = s.makefile('wb')
    print("Connection Open")
    try:

        time.sleep(2)

    finally:
        #    camera = picamera.PiCamera()
        camera.stop_preview()

    with PiCamera() as camera:
        # Forced sensor mode, 1640x1232, full FoV. See:
        # https://picamera.readthedocs.io/en/release-1.13/fov.html#sensor-modes
        # This is the resolution inference run on.
        camera.sensor_mode = 4

        # Scaled and cropped resolution. If different from sensor mode implied
        # resolution, inference results must be adjusted accordingly. This is
        # true in particular when camera.start_recording is used to record an
        # encoded h264 video stream as the Pi encoder can't encode all native
        # sensor resolutions, or a standard one like 1080p may be desired.
        camera.resolution = (1640, 1232)

        # Start the camera stream.
        camera.framerate = 30
        camera.start_preview()
        stream = io.BytesIO()
        camera.capture(stream, format='mjpeg')
        connection.write(struct.pack('<L', stream.tell()))
        connection.flush()
        stream.seek(0)
        connection.write(stream.read())
        stream.seek(0)
        stream.truncate()
        with CameraInference(image_classification.model()) as inference:
            for i, result in enumerate(inference.run()):
                if i == args.num_frames:
                    break
                classes = image_classification.get_classes(result)
                print_classes(classes, args.num_objects)

        camera.stop_preview()
        connection.close()
        s.close()


if __name__ == '__main__':
    main()



