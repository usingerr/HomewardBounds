# HomewardBounds
  Homeward Bounds is an application created by Ross Usinger, Giles Holmes, and David Vehapetian for their senior year project at Wentworth Insitute of Technology. The aim is to use one Raspberry Pi to track another Raspberry Pi in a given household, and eventually to create a product that will allow dog owners to keep their dogs out of rooms they want to stay dog-free. 

  The basic idea is allowing and banning certain areas in your house, and, in so doing, training the hub-pi to identify a banned or allowed area based on a combination of signal strengths to and from the collar-pi. It is necessary to ban areas individually, and possibly necessary for the allowed area to be done in one go.

## Required hardware
- Two raspberry pis, preferably full-on pis, and not zeros.
- either a portable battery or very long power cable for one of the pis
- a place with a power source to affix one of the pis as the hub

## To train
- place the "hub" pi in the room where it can stay; a room on one side of a house or building (providing it is not too large) is the best   for training purposes.

- begin the trainingServer.py python script on the hub; if you are going to ban a room, the command should look something like:
    sudo python trainingServer.py NO 1
      where the "1" is incrememted by one for each new banned room
  if you want to allow the areas in between the banned ones, the command should be:
    sudo python trainingServer.py YES 1
      I'm fairly certain that you *can* create multiple allowed areas; let me know 

- bring the "collar" (the other pi) into an area that you would like to ban, or that you would like to allow; begin the training. you can   use the same trainingAndRealtimeClient.py. all you need to do is run the client on the collar

- walk the collar around the banned/allowed area for a minute or so, preferably at dog-height, so as to get the most similar readings as     when it will be on your dog; it could also work to let your dog roam around that area for the same amount of time.

(instructions for running actual thing TBD)
