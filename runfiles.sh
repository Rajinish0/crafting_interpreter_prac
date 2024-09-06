#! /bin/bash

shopt -s expand_aliases
source import_alias.sh

# files=("simple.lox" "function.lox" "breakandfix.lox" "classes.lox")
# for file in "${files[@]}"
for file in *.lox; do

	echo "Running file: $file"
    #java -cp lox/bin lox.Lox "$file"
	jlox $file

	errCode=$?
    
    if [ $errCode -ne 0 ]; then
        echo "Error: $file failed with exit code $errCode"
        exit 1
    fi
done

echo "All files executed successfully."

