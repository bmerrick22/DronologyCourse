#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
© Copyright 2015-2016, 3D Robotics.
simple_goto.py: GUIDED mode "simple goto" example (Copter Only)
Demonstrates how to arm and takeoff in Copter and how to navigate to points using Vehicle.simple_goto.
Full documentation is provided at http://python.dronekit.io/examples/simple_goto.html
"""

import time
from dronekit_sitl import SITL
from dronekit import Vehicle, VehicleMode, connect, LocationGlobalRelative


from websocket import create_connection
from drone_model import Drone_Model

ws = create_connection("ws://localhost:8000")

# drone_count = 1

# drone_models={}

def connect_virtual_vehicle(instance, home):
    sitl = SITL()
    sitl.download('copter', '3.3', verbose=True)
    instance_arg = '-I%s' %(str(instance))
    home_arg = '--home=%s, %s,%s,180' % (str(home[0]), str(home[1]), str(home[2]))
    sitl_args = [instance_arg, '--model', 'quad', home_arg]
    sitl.launch(sitl_args, await_ready=True)
    tcp, ip, port = sitl.connection_string().split(':')
    port = str(int(port) + instance * 10)
    conn_string = ':'.join([tcp, ip, port])
    print('Connecting to vehicle on: %s' % conn_string)

    vehicle = connect(conn_string)
    vehicle.wait_ready(timeout=120)
    print("Reached here")

    data_model = Drone_Model(instance+1,home[0],home[1])
    # drone_model[drone_count] = drone_model
    
    

    return vehicle, sitl, data_model


def custom_sleep(vehicle,drone_model, sleep_time):
        current_time = 0
        while(current_time<sleep_time):
            drone_model.update_status(vehicle.location.global_relative_frame.lat, vehicle.location.global_relative_frame.lon)
            ws.send(drone_model.toJSON())
            time.sleep(1)
            current_time+=1




def arm_and_takeoff(aTargetAltitude, vehicle, data_model):
    """
    Arms vehicle and fly to aTargetAltitude.
    """

    print("Basic pre-arm checks")
    # Don't try to arm until autopilot is ready
    while not vehicle.is_armable:
        print(" Waiting for vehicle to initialise...")
        time.sleep(3)
    print("Arming motors")
    vehicle.mode = VehicleMode("GUIDED")
    vehicle.armed = True

    while not vehicle.armed:
        print(" Waiting for arming...")
        time.sleep(1)
    
    print("Vehicle armed!")
    print("Taking off!")
    vehicle.simple_takeoff(aTargetAltitude)  # Take off to target altitude

    # Wait until the vehicle reaches a safe height before processing the goto
    #  (otherwise the command after Vehicle.simple_takeoff will execute
    #   immediately).
    while True:
        print(" Altitude: ", vehicle.location.global_relative_frame.alt)
        # Break and return from function just below target altitude.
        
        if vehicle.location.global_relative_frame.alt >= aTargetAltitude * 0.95:
            print("Reached target altitude")
            break
        # time.sleep(1)
        custom_sleep(vehicle,data_model,1)

def fly_drone(vehicle, data_model):
    arm_and_takeoff(10, vehicle, data_model)
    print("Set default/target airspeed to 3")
    vehicle.airspeed = 3

    print("Going towards first point for 30 seconds ...")
    point1 = LocationGlobalRelative(-35.361354, 149.165218, 20)
    print (point1)
    vehicle.simple_goto(point1)

    # sleep so we can see the change in map
    #time.sleep(10)
    custom_sleep(vehicle,data_model,10)
    print("Going towards second point for 30 seconds (groundspeed set to 10 m/s) ...")
    point2 = LocationGlobalRelative(-35.363244, 149.168801, 20)
    vehicle.simple_goto(point2, groundspeed=10)

    # sleep so we can see the change in map
    #time.sleep(10)
    custom_sleep(vehicle,data_model,10)

    print("Returning to Launch")
    vehicle.mode = VehicleMode("LAND")
    print (vehicle.location.global_frame)

    # Close vehicle object before exiting script
    print("Close vehicle object")
    vehicle.close()

vehicle, sitl, data_model_1 = connect_virtual_vehicle(0,([41.715446209367,-86.242847096132,0]))
vehicle2, sitl2, data_model_2 = connect_virtual_vehicle(1,([41.715469, -86.242543,0]))

print("flying drone 1")
#arm_and_takeoff(10, vehicle, data_model_1)
fly_drone(vehicle, data_model_1)

print("flying drone 2")
#arm_and_takeoff(10, vehicle2, data_model_2)
fly_drone(vehicle2, data_model_2)


# Shut down simulator if it was started.
if sitl:
    sitl.stop()
    sitl2.stop()
