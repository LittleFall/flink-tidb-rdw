while [ 0 -eq 0 ] 
do
    mysql -htidb -uroot -P4000 -e'source /initsql/tidb-init.sql' >/dev/null 2>&1
    if [ $? -eq 0 ]; then
        break;
    else
        sleep 2
    fi
done