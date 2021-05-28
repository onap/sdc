This folder purpose is to contain any models that needs its imports file updated during the 
initialization of an upgrade.

```bash
├── upgrade
│   ├── <model_1_to_upgrade>
│   │   ├── payload.json
│   │   ├── imports
│   │   │   ├── **/*.yaml
[...]
│   ├── <model_n_to_upgrade>
│   │   ├── payload.json
│   │   ├── imports
│   │   │   ├── **/*.yaml
```

The model folder name is irrelevant to indicate which model should be updated. This information must
be provided in the payload.json contained within the model folder.

The content of the payload.json for the model with name "ETSI SOL001 v2.5.1" is expected to be:
```json
{
  "name": "ETSI SOL001 v2.5.1"
}
```

