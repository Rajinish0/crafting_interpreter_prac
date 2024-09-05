#! /bin/bash

shopt -s expand_aliases
source import_alias.sh

# files=("simple.lox" "function.lox" "breakandfix.lox" "classes.lox")
#files=$(find *.lox)
#"${files[@]}"
for file in *.lox; do

	echo "Running file: $file"
    # Run the file
    #java -cp lox/bin lox.Lox "$file"
	jlox $file

	errCode=$?
    
    # Check the exit code
    if [ $errCode -ne 0 ]; then
        echo "Error: $file failed with exit code $errCode"
        exit 1
    fi
done

echo "All files executed successfully."

