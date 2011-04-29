#!/bin/bash

if [ ! -f $HOME/pract06/Escape ]; then
  cp $HOME/pract05/Escape $HOME/pract06/
fi

export LD_LIBRARY_PATH=$HOME/pract06:.

./Escape
