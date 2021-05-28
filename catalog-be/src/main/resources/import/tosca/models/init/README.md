This folder purpose is to contain any models that needs to be created during the first 
initialization of the system (install).

```bash
├── init
│   ├── <model_1_to_create>
│   │   ├── payload.json
│   │   ├── imports
│   │   │   ├── **/*.yaml
[...]
│   ├── <model_n_to_create>
│   │   ├── payload.json
│   │   ├── imports
│   │   │   ├── **/*.yaml
```

The model folder name is irrelevant to indicate which model should be created. This information must
be provided in the payload.json contained within the model folder.

The content of the payload.json for the model with name "ETSI SOL001 v2.5.1" is expected to be:
```json
{
  "name": "ETSI SOL001 v2.5.1"
}
```

