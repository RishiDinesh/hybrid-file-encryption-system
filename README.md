# hybrid-file-encryption-system

## Aim
This project presents a novel file encryption system involving a hybrid of a modified AES and Blowfish algorithms. The AES algorithm has been modified by altering the SubBytes step to include a function that swaps two halves of every element of the state array prior to the Substitution step. The Blowfish algorithm, on the other hand, has been modified by generating a 16-bit binary vector that controls which one of two modified F-functions has to be used in each round. The encrypted halves of the two algorithms are then combined to produce the final ciphertext. Results show that the proposed system outperforms the Blowfish algorithm in terms of speed and throughput but lags behind AES in the same aspect. The avalanche effect, however, is almost the same as the standard AES and Blowfish algorithms (93.5%). This shows that the hybrid encryption algorithm added a layer of complexity while maintaining the overall security performance of the system.

## System Design
 - Figure below llustrates the workflow of the proposed system. Given an input text, Plaintext0, the system first splits it into two equal halves, Plaintext1 and Plaintext2. These two halves are then fed into the modified AES and Blowfish algorithm respectively. The keys for these two algorithms are provided by the Key Management Unit (KMU). It is the responsibility of the user to keep the KMU secured. 
 - The AES algorithm uses an Initialization Vector whereas the Blowfish algorithm uses a 16-bit binary vector. These two vectors are randomly generated for each encryption process and appended to the resulting ciphertext. The result of the two algorithms, Ciphertext1 and Ciphertext2, are then combined to produce the final ciphertext Ciphertext0.
 - The decryption process is simply the inverse of the above workflow. First, the Initialization Vector and 16-bit binary vector is extracted from the ciphertext. The ciphertext is then split into two and decrypted using AES and Blowfish. The results of decryption are then combined to produce the original plaintext.  
![image](https://user-images.githubusercontent.com/63601038/179464597-1bb637e2-e4da-49b6-bc27-c0f6ee805f3a.png)

## Results
We can see that the speed of the proposed system is roughly midway between that of AES and Blowfish. This can be attributed to the fact that the proposed system is simply a combination of modified versions of the standard algorithms and thus produces an averaged performance of the two algorithms.

![image](https://user-images.githubusercontent.com/63601038/179464886-da853346-245c-464a-85d2-fa3274257fff.png)




