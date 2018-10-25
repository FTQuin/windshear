Windshear is an aerial refueling game.
AUTHOR: tewky @ github.com
STATUS: PRE-ALPHA


It's the throwaway prototype of a 3D flight game I will be building.

How to play:

Run test.bat from a Windows machine*

* Other platforms are not supported right now because of the in-progress sound module
that uses unsupported features on some Linux distros

Right click and drag left/ right to control throttle
 - Or press A/D to adjust throttle
Left click and drag up and down to control yoke - drag in the direction you want those nose to pitch

Default level:
1. Lower flaps to setting 2 (press V twice)
2. Apply maximum thrust
3. At 150 - 160 knots, pitch upwards (by dragging left click upwards)
4. Gently lift off, careful not to strike the tail
5. Climb quickly! Mountains are ahead
   5 - Raise landing gear by pressing "G"
   5 - Raise flaps one notch at a time by pressing "F"
6. Fly the waypoints while climbing to 3000 meters
7. At 3000 meters, press "S" to switch navigation target to the tanker aircraft
8. Adjust your flight path to line up behind the tanker
9. By matching speeds and making fine adjustments, position the nose of your aircraft
	next to the fuel boom nozzle.
10. When the nozzle is connected, hold "W" to transfer fuel. Maintain connection by
     flying the same speed and trying to remain in the same relative position to the tanker
	 as when it connected.
11. After the fuel tank is full, press "S" again to switch back to the flight plan navigation.
12. Fly the waypoints.
13. During approach, gradually descend and slow down as you glide towards the Landing waypoint.
14. Lower the landing gear by pressing "G".
15. As you slow down, press "V" to lower flaps. Do it one step at a time and adjust your flight path
    for optimal glide.
16. ***UNFINISHED ***
16. After touchdown, immediately cut thrust to minimum, press "B" to activate speed brakes,
     then press "Space" to activate wheel brakes.
17. Once you slow down to taxi speed, if you are still on the runway and the fuel transfer
     tank is full, a win message will be displayed


CONTRIBUTORS:

This application contains code I wrote as a first-year CS student in 2011. I've used the
development of this game to help learn concepts, data structures, and coding practices.
As a result the class structure is inconsistent and the code messy. I will be refactoring everything
and creating documentation once I finish the remaining features, which are:

- Sound module
- CAS warnings
- Crash animation
- ...

What can be improved right now:
- Flight model
- Cloud generation
- Level configuration
- Sprites
- Control scheme
- Collision detection

Where to start:
- Open crj_200.txt and start playing with the values there (restart game between changes)
   to see how the flight model works (press "U" to see hidden indicators)
- Open levels/fuel_transfer.txt and tweak starting positions, waypoints, etc.
   *** NOTE *** For some yet-to-be-determined reason the starting position of a mountain affects
    the segmentation of its collision mesh, resulting in big gaps of coverage for some starting values.
	Will be fixed soon.







