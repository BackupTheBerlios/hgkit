import logging
import os
import time

def getRepoId(path):
    h = os.popen("hg id -i %s" % path)
    try:
        return h.read().strip()
    finally:
        h.close()

def main():
    while True:
        localid = getRepoId(".")
        remoteid = getRepoId("http://hg.berlios.de/repos/hgkit")
        logging.info("localid=%s, remoteid=%s" % (localid, remoteid))
        if localid != remoteid:
            os.system("mvn clean site")
        time.sleep(60)

if __name__ == "__main__":
    logging.BASIC_FORMAT = "%(asctime)s:" + logging.BASIC_FORMAT
    logging.basicConfig()
    logging.getLogger().setLevel(logging.INFO)
    main()
